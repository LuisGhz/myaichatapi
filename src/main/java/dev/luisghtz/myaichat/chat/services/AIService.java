package dev.luisghtz.myaichat.chat.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.ChatSummary;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AIService {
  private final OpenAIService openAIService;
  private final ChatRepository chatRepository;
  private final ChatService chatService;
  private final MessageService messageService;

  public ChatsListResponseDto getAllChats() {
    var chats = chatRepository.findAll();
    ChatsListResponseDto chatsListResponseDto = new ChatsListResponseDto();
    chatsListResponseDto.setChats(chats.stream().map(chat -> {
      var chatSummary = new ChatSummary(chat.getId(), chat.getTitle());
      return chatSummary;
    }).collect(Collectors.toList()));
    return chatsListResponseDto;
  }

  public HistoryChatDto getChatHistory(UUID id, Pageable pageable) {
    var chat = chatService.findChatById(id);
    var historyMessages = messageService.getAppMessagesHistory(chat, pageable);
    var appMessageHistory = HistoryChatDto.builder()
        .historyMessages(historyMessages)
        .model(chat.getModel())
        .build();
    return appMessageHistory;
  }

  @Transactional
  public AssistantMessageResponseDto sendNewMessage(NewMessageRequestDto newMessageRequestDto, String fileUrl) {
    Chat chat = chatService.getChat(newMessageRequestDto);
    var isNewChat = chat.getMessages() == null || chat.getMessages().isEmpty();
    List<AppMessage> messages = messageService.getMessagesFromChat(chat);
    var newMessage = createNewUserMessage(newMessageRequestDto, chat);
    newMessage = addImageUrlIfApply(newMessage, fileUrl);
    messages.add(newMessage);
    var chatResponse = openAIService.sendNewMessage(messages, chat.getModel());
    var assistantMessage = createAssistantMessage(chatResponse, chat);
    newMessage.setPromptTokens(assistantMessage.getPromptTokens());
    assistantMessage.setPromptTokens(null);
    messageService.saveAll(List.of(newMessage, assistantMessage));
    var assistantMessageResponseDto = createAssistantMessageDto(chatResponse, chat.getId(), isNewChat);
    if (isNewChat)
      generateAndSetTitleForNewChat(chat, newMessageRequestDto, assistantMessageResponseDto);
    return assistantMessageResponseDto;
  }

  @Transactional
  public void deleteChat(UUID id) {
    var chat = chatService.findChatById(id);
    log.info("Deleting chat with Title: '{}'' and ID: '{}'", chat.getTitle(), id);
    log.info("Deleting messages for chat with ID: '{}'", id);
    messageService.deleteAllByChat(chat);
    chatRepository.delete(chat);
  }

  private AppMessage createNewUserMessage(NewMessageRequestDto newMessageRequestDto, Chat chat) {
    var newMessage = AppMessage.builder()
        .role("User")
        .content(newMessageRequestDto.getPrompt())
        .createdAt(new Date())
        .chat(chat)
        .build();
    return newMessage;
  }

  private AppMessage addImageUrlIfApply(AppMessage message, String imageFileUrl) {
    if (imageFileUrl != null && !imageFileUrl.isEmpty()) {
      message.setImageUrl(imageFileUrl);
    }
    return message;
  }

  private AppMessage createAssistantMessage(ChatResponse chatResponse, Chat chat) {
    var usage = chatResponse.getMetadata().getUsage();
    return AppMessage.builder()
        .role("Assistant")
        .content(chatResponse.getResult().getOutput().getText())
        .createdAt(new Date())
        .promptTokens(usage.getPromptTokens())
        .completionTokens(usage.getCompletionTokens())
        .totalTokens(usage.getTotalTokens())
        .chat(chat)
        .build();
  }

  private AssistantMessageResponseDto createAssistantMessageDto(ChatResponse chatResponse, UUID chatId,
      boolean isNewChat) {
    var usage = chatResponse.getMetadata().getUsage();
    var message = AssistantMessageResponseDto.builder()
        .content(chatResponse.getResult().getOutput().getText())
        .promptTokens(usage.getPromptTokens())
        .completionTokens(usage.getCompletionTokens())
        .totalTokens(usage.getTotalTokens())
        .build();
    if (isNewChat)
      message.setChatId(chatId);
    return message;
  }

  private void generateAndSetTitleForNewChat(Chat chat, NewMessageRequestDto newMessageRequestDto,
      AssistantMessageResponseDto res) {
    String generatedTitle = openAIService.generateTitle(chat, newMessageRequestDto.getPrompt(),
        res.getContent());
    chat.setTitle(generatedTitle);
    chatRepository.save(chat);
    res.setChatTitle(generatedTitle);
  }
}
