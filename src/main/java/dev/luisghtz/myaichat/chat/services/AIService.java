package dev.luisghtz.myaichat.chat.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import dev.luisghtz.myaichat.chat.utils.MessagesUtils;
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
    boolean isNewChat = isChatNew(chat);
    AppMessage userMessage = MessagesUtils.processUserMessage(newMessageRequestDto, chat, fileUrl);
    AppMessage assistantMessage = getAssistantResponse(chat, userMessage);
    AssistantMessageResponseDto responseDto = createAssistantMessageDto(assistantMessage, chat.getId(), isNewChat);
    saveMessages(userMessage, assistantMessage);
    if (isNewChat)
      generateAndSetTitleForNewChat(chat, newMessageRequestDto, responseDto);
    return responseDto;
  }

  private boolean isChatNew(Chat chat) {
    return chat.getMessages() == null || chat.getMessages().isEmpty();
  }

  private AppMessage getAssistantResponse(Chat chat, AppMessage userMessage) {
    List<AppMessage> messages = messageService.getMessagesFromChat(chat);
    messages.add(userMessage);
    var chatResponse = openAIService.sendNewMessage(messages, chat.getModel());
    return MessagesUtils.createAssistantMessage(chatResponse, chat);
  }

  private void saveMessages(AppMessage userMessage, AppMessage assistantMessage) {
    userMessage.setPromptTokens(assistantMessage.getPromptTokens());
    assistantMessage.setPromptTokens(null);
    messageService.saveAll(List.of(userMessage, assistantMessage));
  }

  private AssistantMessageResponseDto createAssistantMessageDto(AppMessage assistantMessage, UUID chatId,
      boolean isNewChat) {
    var message = AssistantMessageResponseDto.builder()
        .content(assistantMessage.getContent())
        .promptTokens(assistantMessage.getPromptTokens())
        .completionTokens(assistantMessage.getCompletionTokens())
        .totalTokens(assistantMessage.getTotalTokens())
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
