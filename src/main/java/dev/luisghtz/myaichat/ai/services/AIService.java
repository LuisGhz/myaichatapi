package dev.luisghtz.myaichat.ai.services;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.ai.models.AIProviderService;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AIService implements AIProviderService {
  private final OpenAIService openAIService;
  private final VertexGeminiService vertexGeminiService;

  @Override
  public ChatResponse sendNewMessage(List<AppMessage> messages, Chat chat) {
    if (chat.getModel().startsWith("gpt-")) {
      log.info("Sending message to OpenAI");
      return openAIService.sendNewMessage(messages, chat);
    } else if (chat.getModel().startsWith("gemini-")) {
      log.info("Sending message to Vertex Gemini");
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