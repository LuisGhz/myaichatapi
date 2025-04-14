package dev.luisghtz.myaichat.chat.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.AppMessageHistory;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class MessageService {
  private final MessageRepository messageRepository;

  public List<AppMessageHistory> getAppMessagesHistory(Chat chat, Pageable pageable) {
    var messages = messageRepository.findAllByChatOrderByCreatedAtDesc(chat, pageable);
    var historyMessages = messages.stream()
        .map(message -> {
          return AppMessageHistory.builder()
              .content(message.getContent())
              .role(message.getRole())
              .image(message.getImageUrl())
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

  public List<AppMessage> getMessagesFromChat(Chat chat) {
    var messages = chat.getMessages();
    if (messages == null || messages.isEmpty()) {
      return new ArrayList<>();
    }
    return messages;
  }

  @Transactional
  public void deleteAllByChat(UUID id) {
    log.info("Deleting messages for chat with ID: '{}'", id);
    messageRepository.deleteAllByChatId(id);
  }
}
