package dev.luisghtz.myaichat.chat.services;

import java.util.Date;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
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

  public Chat getChat(NewMessageRequestDto newMessageRequestDto) {
    Chat chat = null;
    if (newMessageRequestDto.getChatId() != null)
      chat = findChatById(newMessageRequestDto.getChatId());
    if (chat == null) {
      chat = getNewChat(newMessageRequestDto);
    }
    return chat;
  }

  public Chat findChatById(UUID id) {
    return chatRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Chat not found with ID: " + id));
  }

  @Transactional
  public Chat getNewChat(NewMessageRequestDto newMessageRequestDto) {
    var newChat = Chat.builder()
        .title("Title")
        .createdAt(new Date())
        .model(newMessageRequestDto.getModel())
        .build();
    if (newMessageRequestDto.getPromptId() != null || !newMessageRequestDto.getPromptId().isEmpty()) {
      var prompt = customPromptService.findById(newMessageRequestDto.getPromptId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Prompt not found with ID: " + newMessageRequestDto.getPromptId()));
      newChat.setCustomPrompt(prompt);
    }
    return chatRepository.save(newChat);
  }

  @Transactional
  public void deleteChat(UUID id) {
    var chat = findChatById(id);
    log.info("Deleting chat with Title: '{}'' and ID: '{}'", chat.getTitle(), id);
    chatRepository.delete(chat);
  }
}