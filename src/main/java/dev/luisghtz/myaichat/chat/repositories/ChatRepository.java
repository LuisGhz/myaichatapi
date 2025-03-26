package dev.luisghtz.myaichat.chat.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.luisghtz.myaichat.chat.entities.Chat;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
}
