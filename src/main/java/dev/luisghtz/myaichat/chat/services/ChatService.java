package dev.luisghtz.myaichat.chat.services;

import java.util.Date;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatService {
  private final ChatRepository chatRepository;

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
    return chatRepository.save(newChat);
  }
}