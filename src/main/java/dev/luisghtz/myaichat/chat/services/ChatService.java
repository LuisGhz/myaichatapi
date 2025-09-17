package dev.luisghtz.myaichat.chat.services;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.auth.services.JwtService;
import dev.luisghtz.myaichat.auth.services.UserService;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.ChatSummary;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatService {
  private final ChatRepository chatRepository;
  private final CustomPromptService customPromptService;
  private final UserService userService;
  private final AIService aiProviderService;
  private final JwtService jwtService;

  public ChatsListResponseDto getAllChats(String userId) {
    UUID userUUID = UUID.fromString(userId);
    var chats = chatRepository.findAllByUserIdOrderByCreatedAtAsc(userUUID);
    ChatsListResponseDto chatsListResponseDto = new ChatsListResponseDto();
    chatsListResponseDto.setChats(chats.stream()
        .map(chat -> new ChatSummary(chat.getId(), chat.getTitle(), chat.getFav()))
        .collect(Collectors.toList()));
    return chatsListResponseDto;
  }

  public Chat getChat(NewMessageRequestDto newMessageRequestDto, String userId) {
    Chat chat = null;
    if (newMessageRequestDto.getChatId() != null)
      chat = findChatById(newMessageRequestDto.getChatId());
    if (chat == null) {
      chat = getNewChat(newMessageRequestDto, userId);
    }
    updateIsWebSearchModeIfApply(chat, newMessageRequestDto);
    return chat;
  }

  public Chat findChatById(UUID id) {
    return chatRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Chat not found with ID: " + id));
  }

  public void generateAndSetTitleForNewChat(Chat chat, NewMessageRequestDto newMessageRequestDto,
      AssistantMessageResponseDto res) {
  String generatedTitle = aiProviderService.generateTitle(chat, newMessageRequestDto.getContent(),
        res.getContent());
    chat.setTitle(generatedTitle);
    chatRepository.save(chat);
    res.setChatTitle(generatedTitle);
  }

  @Transactional
  public Chat getNewChat(NewMessageRequestDto newMessageRequestDto, String userId) {
    var user = userService.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
    var newChat = Chat.builder()
        .createdAt(new Date())
        .model(newMessageRequestDto.getModel())
        .maxOutputTokens(newMessageRequestDto.getMaxOutputTokens())
        .fav(false)
        .isWebSearchMode(newMessageRequestDto.getIsWebSearchMode())
        .user(user)
        .build();
    if (newMessageRequestDto.getPromptId() != null && !newMessageRequestDto.getPromptId().isEmpty()) {
      var prompt = customPromptService.findById(newMessageRequestDto.getPromptId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Prompt not found with ID: " + newMessageRequestDto.getPromptId()));
      newChat.setCustomPrompt(prompt);
    }
    return save(newChat);
  }

  @Transactional
  public Chat save(Chat chat) {
    return chatRepository.save(chat);
  }

  @Transactional
  public void deleteChat(UUID id, UserJwtDataDto user) {
    var chat = findChatById(id);
    validateChatBelongsToUser(chat, user);
    log.info("Deleting chat with Title: '{}'' and ID: '{}'", chat.getTitle(), id);
    chatRepository.delete(chat);
  }

  @Transactional
  public int renameChatTitleById(UUID id, String title, UserJwtDataDto user) {
    var chat = findChatById(id);
    validateChatBelongsToUser(chat, user);
    log.info("Renaming chat with Title: '{}' and ID: '{}'", chat.getTitle(), id);
    return chatRepository.renameChatTitleById(id, title);
  }

  @Transactional
  public int changeMaxOutputTokens(UUID id, Short maxOutputTokens, UserJwtDataDto user) {
    var chat = findChatById(id);
    validateChatBelongsToUser(chat, user);
    log.info("Changing max output tokens for chat with ID: '{}'", id);
    return chatRepository.changeMaxTokens(id, maxOutputTokens);
  }

  @Transactional
  public int changeIsWebSearchMode(UUID id, Boolean webSearchMode, UserJwtDataDto user) {
    var chat = findChatById(id);
    validateChatBelongsToUser(chat, user);
    log.info("Changing web search mode for chat with ID: '{}'", id);
    return chatRepository.changeWebSearchMode(id, webSearchMode);
  }

  @Transactional
  public void toggleChatFav(UUID id, UserJwtDataDto user) {
    var chat = findChatById(id);
    validateChatBelongsToUser(chat, user);
    log.info("Toggling favorite status for chat with ID: '{}'", id);
    chat.setFav(!chat.getFav());
    chatRepository.setChatFav(id, chat.getFav());
  }

  private void validateChatBelongsToUser(Chat chat, UserJwtDataDto user) {
    if (!chat.getUser().getId().equals(UUID.fromString(user.getId()))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this chat");
    }
  }

  private void updateIsWebSearchModeIfApply(Chat chat, NewMessageRequestDto newMessageRequestDto) {
    if (newMessageRequestDto.getIsWebSearchMode() != null
        && !newMessageRequestDto.getIsWebSearchMode().equals(chat.getIsWebSearchMode())) {
      chat.setIsWebSearchMode(newMessageRequestDto.getIsWebSearchMode());
      chatRepository.save(chat);
    }
  }
}