package dev.luisghtz.myaichat.chat.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.ChatSummary;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIService {
  private final OpenAIService openAIService;
  private final ChatRepository chatRepository;
  private final MessageRepository messageRepository;

  public ChatsListResponseDto getAllChats() {
    var chats = chatRepository.findAll();
    ChatsListResponseDto chatsListResponseDto = new ChatsListResponseDto();
    chatsListResponseDto.setChats(chats.stream().map(chat -> {
      var chatSummary = new ChatSummary(chat.getId(), chat.getTitle());
      return chatSummary;
    }).collect(Collectors.toList()));
    return chatsListResponseDto;
  }

  public AssistantMessageResponseDto sendNewMessage(NewMessageRequestDto newMessageRequestDto) {
    Chat chat = getChat(newMessageRequestDto);
    var isNewChat = chat.getMessages().size() == 0;
    var messages = messageRepository.findAllByChat(chat);
    var newMessage = createNewUserMessage(newMessageRequestDto, chat);
    messages.add(newMessage);
    var chatResponse = openAIService.sendNewMessage(messages, chat.getModel());
    var assistantMessage = createAssistantMessage(chatResponse, chat);
    messageRepository.saveAll(List.of(newMessage, assistantMessage));
    var assistantMessageResponseDto = createAssistantMessageDto(chatResponse, chat.getId(), isNewChat);
    return assistantMessageResponseDto;
  }

  private Chat getChat(NewMessageRequestDto newMessageRequestDto) {
    Chat chat = null;
    if (newMessageRequestDto.getChatId() != null)
      chat = findChatById(newMessageRequestDto.getChatId());
    if (chat == null) {
      chat = getNewChat(newMessageRequestDto);
    }
    return chat;
  }

  private Chat findChatById(UUID id) {
    return chatRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Chat not found with ID: " + id));
  }

  private Chat getNewChat(NewMessageRequestDto newMessageRequestDto) {
    var newChat = Chat.builder()
        .title("Title")
        .createdAt(new Date())
        .model(newMessageRequestDto.getModel())
        .build();
    return chatRepository.save(newChat);
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
}
