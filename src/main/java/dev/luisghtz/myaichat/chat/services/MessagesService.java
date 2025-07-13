package dev.luisghtz.myaichat.chat.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
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

  public HistoryChatDto getPreviousMessages(UUID id, Pageable pageable) {
    var chat = chatService.findChatById(id);
    var tokens = getSumOfPromptAndCompletionTokensByChatId(id);
    var historyMessages = getChatPreviousMessages(chat, pageable);
    var appMessageHistory = HistoryChatDto.builder()
        .historyMessages(historyMessages)
        .model(chat.getModel())
        .totalPromptTokens(tokens.getPromptTokens())
        .totalCompletionTokens(tokens.getCompletionTokens())
        .maxOutputTokens(chat.getMaxOutputTokens())
        .build();
    return appMessageHistory;
  }

  @Transactional
  public AssistantMessageResponseDto sendNewMessage(NewMessageRequestDto newMessageRequestDto, String fileUrl) {
    Chat chat = chatService.getChat(newMessageRequestDto);
    boolean isNewChat = isChatNew(chat);
    AppMessage userMessage = MessagesUtils.processUserMessage(newMessageRequestDto, chat, fileUrl);
    AppMessage assistantMessage = getAssistantResponse(chat, userMessage);
    AssistantMessageResponseDto responseDto = createAssistantMessageDto(assistantMessage, chat.getId(), isNewChat);
    saveMessages(userMessage, assistantMessage);
    if (isNewChat) {
      chatService.generateAndSetTitleForNewChat(chat, newMessageRequestDto, responseDto);
      addNewChatDataToFirstMessage(responseDto, chat);
    }
    var tokens = getSumOfPromptAndCompletionTokensByChatId(chat.getId());
    responseDto.setTotalChatPromptTokens(tokens.getPromptTokens());
    responseDto.setTotalChatCompletionTokens(tokens.getCompletionTokens());
    return responseDto;
  }

  private boolean isChatNew(Chat chat) {
    return chat.getMessages() == null || chat.getMessages().isEmpty();
  }

  private AppMessage getAssistantResponse(Chat chat, AppMessage userMessage) {
    List<AppMessage> messages = getMessagesFromChat(chat);
    messages.add(userMessage);
    var chatResponse = aiProviderService.sendNewMessage(messages, chat);
    return MessagesUtils.createAssistantMessage(chatResponse, chat);
  }

  private void saveMessages(AppMessage userMessage, AppMessage assistantMessage) {
    userMessage.setPromptTokens(assistantMessage.getPromptTokens());
    assistantMessage.setPromptTokens(null);
    saveAll(List.of(userMessage, assistantMessage));
  }

  private AssistantMessageResponseDto createAssistantMessageDto(AppMessage assistantMessage, UUID chatId,
      boolean isNewChat) {
    var message = AssistantMessageResponseDto.builder()
        .content(assistantMessage.getContent())
        .promptTokens(assistantMessage.getPromptTokens())
        .completionTokens(assistantMessage.getCompletionTokens())
        .totalTokens(assistantMessage.getTotalTokens())
        .build();
    return message;
  }

  private void addNewChatDataToFirstMessage(AssistantMessageResponseDto responseDto, Chat chat) {
    responseDto.setChatId(chat.getId());
    responseDto.setChatTitle(chat.getTitle());
  }

  private List<AppMessageHistory> getChatPreviousMessages(Chat chat, Pageable pageable) {
    var messages = messageRepository.findAllByChatOrderByCreatedAtDesc(chat, pageable);
    var historyMessages = messages.stream()
        .map(message -> {
          return AppMessageHistory.builder()
              .content(message.getContent())
              .role(message.getRole())
              .image(message.getFileUrl())
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
  public void deleteAllByChat(UUID id) {
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
}
