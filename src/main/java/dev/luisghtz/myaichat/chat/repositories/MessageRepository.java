package dev.luisghtz.myaichat.chat.repositories;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.TokensSum;

import java.util.List;

public interface MessageRepository extends JpaRepository<AppMessage, UUID> {
  List<AppMessage> findAllByChat(Chat chat);

  List<AppMessage> findAllByChatOrderByCreatedAtDesc(Chat chat, Pageable pageable);

  void deleteAllByChatId(UUID chatId);

  @Query("SELECT new dev.luisghtz.myaichat.chat.models.TokensSum(COALESCE(SUM(m.promptTokens), 0), COALESCE(SUM(m.completionTokens), 0)) FROM AppMessage m WHERE m.chat.id = :chatId")
  TokensSum getSumOfPromptAndCompletionTokensByChatId(@Param("chatId") UUID chatId);

  List<AppMessage> findAllByChatId(UUID id);
}
