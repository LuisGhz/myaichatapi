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

  List<Chat> findAllByUserIdOrderByCreatedAtAsc(@Param("userId") UUID userId);

  @Modifying
  @Query("UPDATE Chat c SET c.title = :title WHERE c.id = :id")
  int renameChatTitleById(@Param("id") UUID id, @Param("title") String title);

  @Modifying
  @Query("UPDATE Chat c SET c.maxOutputTokens = :maxOutputTokens WHERE c.id = :id")
  int changeMaxTokens(@Param("id") UUID id, @Param("maxOutputTokens") Short maxOutputTokens);

  @Modifying
  @Query("UPDATE Chat c SET c.fav = :fav WHERE c.id = :id")
  int setChatFav(@Param("id") UUID id, @Param("fav") Boolean fav);
}
