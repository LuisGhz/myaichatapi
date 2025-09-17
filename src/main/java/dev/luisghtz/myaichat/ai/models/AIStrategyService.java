package dev.luisghtz.myaichat.ai.models;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import reactor.core.publisher.Flux;

public interface AIStrategyService {
  Flux<ChatResponse> getAssistantMessage(List<AppMessage> messages, Chat chat);

  String generateTitle(Chat chat, String userMessage, String assistantMessage);
}
