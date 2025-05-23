package dev.luisghtz.myaichat.ai.models;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;

public interface AIProviderService {
  ChatResponse sendNewMessage(List<AppMessage> messages, Chat chat);

  String generateTitle(String userMessage, String assistantMessage);
}
