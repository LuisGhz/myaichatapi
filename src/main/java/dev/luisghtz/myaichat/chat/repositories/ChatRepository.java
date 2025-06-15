package dev.luisghtz.myaichat.chat.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import dev.luisghtz.myaichat.chat.entities.Chat;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
  List<Chat> findAllByOrderByCreatedAtAsc();

  @Modifying
  @Query("UPDATE Chat c SET c.title = :title WHERE c.id = :id")
  int renameChatTitleById(@Param("id") UUID id, @Param("title") String title);
}
