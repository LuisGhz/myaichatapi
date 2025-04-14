package dev.luisghtz.myaichat.chat.repositories;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;

import java.util.List;

public interface MessageRepository extends JpaRepository<AppMessage, UUID> {
  List<AppMessage> findAllByChat(Chat chat);

  List<AppMessage> findAllByChatOrderByCreatedAtDesc(Chat chat, Pageable pageable);

  void deleteAllByChatId(UUID chatId);
}
