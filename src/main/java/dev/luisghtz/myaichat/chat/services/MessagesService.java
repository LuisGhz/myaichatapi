package dev.luisghtz.myaichat.chat.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.chat.dtos.AssistantResponseDto;
import dev.luisghtz.myaichat.chat.dtos.UserMessageResDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.AppMessageHistory;
import dev.luisghtz.myaichat.chat.models.TokensSum;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.utils.MessagesUtils;
import dev.luisghtz.myaichat.file.providers.AwsS3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Log4j2
public class MessagesService {
  private final AIService aiProviderService;
  private final ChatService chatService;
  private final MessageRepository messageRepository;
  private final AwsS3Service awsS3Service;

  @Value("${cdn}")
  private String cdn;

  public HistoryChatDto getPreviousMessages(UUID id, Pageable pageable, UserJwtDataDto user) {
    var chat = chatService.findChatById(id);
    validateIfChatBelongsToUser(chat, user);
    var tokens = getSumOfPromptAndCompletionTokensByChatId(id);
    var historyMessages = getChatPreviousMessages(chat, pageable);
    var appMessageHistory = HistoryChatDto.builder()
        .historyMessages(historyMessages)
        .model(chat.getModel())
        .totalPromptTokens(tokens.getPromptTokens())
        .totalCompletionTokens(tokens.getCompletionTokens())
        .maxOutputTokens(chat.getMaxOutputTokens())
        .isWebSearchMode(chat.getIsWebSearchMode())
        .build();
    return appMessageHistory;
  }

  @Transactional
  public UserMessageResDto userMessage(NewMessageRequestDto newMessageRequestDto, UserJwtDataDto user, String fileUrl) {
    Chat chat = chatService.getChat(newMessageRequestDto, user.getId());
    boolean isNew = isChatNew(chat);
    AppMessage userMessage = MessagesUtils.processUserMessage(newMessageRequestDto, chat, fileUrl);
    messageRepository.save(userMessage);
    var res = new UserMessageResDto(isNew, chat.getId().toString());
    return res;
  }

  public Flux<AssistantResponseDto> getAssistantMessage(UUID chatId, UserJwtDataDto user) {
    // Get the chat and validate ownership
    Chat chat = chatService.findChatById(chatId);
    validateIfChatBelongsToUser(chat, user);
    
    // Get all messages from the chat
    List<AppMessage> messages = getMessagesFromChat(chat);
    
    // Check if this is a new chat (no messages yet)
    boolean isNewChat = messages.isEmpty();
    
    // Create atomic references to accumulate response data
    AtomicReference<StringBuilder> contentBuilder = new AtomicReference<>(new StringBuilder());
    AtomicReference<Integer> promptTokens = new AtomicReference<>();
    AtomicReference<Integer> completionTokens = new AtomicReference<>();
    AtomicReference<Integer> totalTokens = new AtomicReference<>();
    AtomicReference<AppMessage> lastUserMessage = new AtomicReference<>();
    AtomicReference<Boolean> isLastChunk = new AtomicReference<>(false);
    
    // Get the last user message to update with tokens later
    if (!messages.isEmpty()) {
      for (int i = messages.size() - 1; i >= 0; i--) {
        if ("User".equals(messages.get(i).getRole())) {
          lastUserMessage.set(messages.get(i));
          break;
        }
      }
    }
    
    // Get streaming response from AI service
    return aiProviderService.getAssistantMessage(messages, chat)
        .map(chatResponse -> {
          // Extract content from response
          String content = chatResponse.getResult().getOutput().getText();
          
          // Store usage metadata from the response (only available in last chunk)
          var usage = chatResponse.getMetadata().getUsage();
          if (usage != null) {
            promptTokens.set(usage.getPromptTokens());
            completionTokens.set(usage.getCompletionTokens());
            totalTokens.set(usage.getTotalTokens());
            isLastChunk.set(true);
            log.info("Last chunk detected - Prompt tokens: {}, Completion tokens: {}, Total tokens: {}", 
                promptTokens.get(), completionTokens.get(), totalTokens.get());
          }
          
          // Accumulate content
          contentBuilder.get().append(content);
          
          // If this is the last chunk, prepare complete response with all data
          if (isLastChunk.get()) {
            // Async save to database without blocking response
            Mono.fromRunnable(() -> {
              try {
                saveAssistantMessageAndUpdateTokensAsync(
                    chat, 
                    contentBuilder.get().toString(), 
                    promptTokens.get(), 
                    completionTokens.get(), 
                    totalTokens.get(),
                    lastUserMessage.get(),
                    isNewChat
                );
              } catch (Exception e) {
                log.error("Error saving assistant message: ", e);
              }
            }).subscribeOn(Schedulers.boundedElastic()).subscribe();
            
            // Generate title if needed (also async)
            String generatedTitle = null;
            if (isNewChat && chat.getTitle() == null && lastUserMessage.get() != null) {
              try {
                generatedTitle = aiProviderService.generateTitle(chat, lastUserMessage.get().getContent(), contentBuilder.get().toString());
                final String titleToSave = generatedTitle;
                Mono.fromRunnable(() -> {
                  try {
                    chatService.updateChatTitle(chat.getId(), titleToSave);
                  } catch (Exception e) {
                    log.error("Error updating chat title: ", e);
                  }
                }).subscribeOn(Schedulers.boundedElastic()).subscribe();
              } catch (Exception e) {
                log.error("Error generating chat title: ", e);
              }
            }
            
            // Get total chat tokens
            TokensSum tokens = getSumOfPromptAndCompletionTokensByChatId(chatId);
            
            // Return complete response for last chunk
            return AssistantResponseDto.builder()
                .content(content)
                .isLastChunk(true)
                .chatId(chat.getId())
                .chatTitle(generatedTitle != null ? generatedTitle : chat.getTitle())
                .promptTokens(promptTokens.get())
                .completionTokens(completionTokens.get())
                .totalTokens(totalTokens.get())
                .totalChatPromptTokens(tokens != null ? tokens.getPromptTokens() : 0L)
                .totalChatCompletionTokens(tokens != null ? tokens.getCompletionTokens() : 0L)
                .build();
          } else {
            // Return chunk response for non-last chunks
            return AssistantResponseDto.builder()
                .content(content)
                .isLastChunk(false)
                .build();
          }
        })
        .doOnError(error -> {
          log.error("Error during AI response generation: ", error);
        });
  }
  
  private void saveAssistantMessageAndUpdateTokensAsync(Chat chat, String content, Integer promptTokens, 
      Integer completionTokens, Integer totalTokens, AppMessage lastUserMessage, boolean isNewChat) {
    
    // Create and save assistant message
    AppMessage assistantMessage = AppMessage.builder()
        .role("Assistant")
        .content(content)
        .createdAt(new java.util.Date())
        .completionTokens(completionTokens)
        .totalTokens(totalTokens)
        .chat(chat)
        .build();
    
    messageRepository.save(assistantMessage);
    
    // Update the last user message with prompt tokens
    if (lastUserMessage != null && promptTokens != null) {
      lastUserMessage.setPromptTokens(promptTokens);
      messageRepository.save(lastUserMessage);
    }
    
    log.info("Assistant message saved successfully for chat: {}", chat.getId());
  }

  private boolean isChatNew(Chat chat) {
    return chat.getMessages() == null || chat.getMessages().isEmpty();
  }

  private List<AppMessageHistory> getChatPreviousMessages(Chat chat, Pageable pageable) {
    var messages = messageRepository.findAllByChatOrderByCreatedAtDesc(chat, pageable);
    var historyMessages = messages.stream()
        .map(message -> {
          return AppMessageHistory.builder()
              .content(message.getContent())
              .role(message.getRole())
              .file(message.getFileUrl())
              .promptTokens(message.getPromptTokens())
              .completionTokens(message.getCompletionTokens())
              .build();
        }).collect(Collectors.toList());
    Collections.reverse(historyMessages);

    return historyMessages;
  }

  @Transactional
  public List<AppMessage> saveAll(Iterable<AppMessage> messages) {
    return messageRepository.saveAll(messages);
  }

  @Transactional
  public void deleteAllByChat(UUID id, UserJwtDataDto user) {
    var chat = chatService.findChatById(id);
    validateIfChatBelongsToUser(chat, user);
    log.info("Deleting messages for chat with ID: '{}'", id);
    var messages = messageRepository.findAllByChatId(id);
    var files = messages.stream()
        .map(AppMessage::getFileUrl)
        .filter(fileUrl -> fileUrl != null && !fileUrl.isEmpty())
        .distinct()
        .collect(Collectors.toList());
    files.forEach(this::removeFileFromS3);
    messageRepository.deleteAllByChatId(id);
  }

  private List<AppMessage> getMessagesFromChat(Chat chat) {
    var messages = chat.getMessages();
    if (messages == null || messages.isEmpty()) {
      return new ArrayList<>();
    }
    return messages;
  }

  private TokensSum getSumOfPromptAndCompletionTokensByChatId(UUID chatId) {
    return messageRepository.getSumOfPromptAndCompletionTokensByChatId(chatId);
  }

  private void removeFileFromS3(String fileUrl) {
    var cleanUrl = fileUrl.replace(cdn, "");
    awsS3Service.deleteFile(cleanUrl);
  }

  private void validateIfChatBelongsToUser(Chat chat, UserJwtDataDto user) {
    if (!chat.getUser().getId().equals(UUID.fromString(user.getId())))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this chat");
  }
}
