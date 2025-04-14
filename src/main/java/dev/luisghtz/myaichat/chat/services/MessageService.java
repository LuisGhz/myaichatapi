package dev.luisghtz.myaichat.chat.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.AppMessageHistory;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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

  public List<AppMessage> saveAll(Iterable<AppMessage> messages) {
    return messageRepository.saveAll(messages);
  }

  public void deleteAllByChat(Chat chat) {
    messageRepository.deleteAllByChat(chat);
  }
}
