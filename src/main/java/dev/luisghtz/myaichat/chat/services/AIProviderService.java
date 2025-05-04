package dev.luisghtz.myaichat.chat.services;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.ProviderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIProviderService implements ProviderService {
  private final OpenAIService openAIService;
  private final VertexGeminiService vertexGeminiService;

  @Override
  public ChatResponse sendNewMessage(List<AppMessage> messages, Chat chat) {
    if (chat.getModel().startsWith("gpt-")) {
      return openAIService.sendNewMessage(messages, chat);
    } else if (chat.getModel().startsWith("gemini-")) {
      return vertexGeminiService.sendNewMessage(messages, chat);
    } else {
      throw new UnsupportedOperationException("Unsupported model: " + chat.getModel());
    }
  }

  public String generateTitle(Chat chat, String userMessage, String assistantMessage) {
    if (chat.getModel().startsWith("gpt-")) {
      return openAIService.generateTitle(chat, userMessage, assistantMessage);
    } else if (chat.getModel().startsWith("gemini-")) {
      return vertexGeminiService.generateTitle(chat, userMessage, assistantMessage);
    } else {
      throw new UnsupportedOperationException("Unsupported model: " + chat.getModel());
    }
  }

}