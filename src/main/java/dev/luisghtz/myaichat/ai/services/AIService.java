package dev.luisghtz.myaichat.ai.services;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.ai.models.AIStrategyService;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Log4j2
public class AIService implements AIStrategyService {
  private final OpenAIService openAIService;
  private final VertexGeminiService vertexGeminiService;

  @Override
  public Flux<ChatResponse> getAssistantMessage(List<AppMessage> messages, Chat chat) {
    if (chat.getModel().startsWith("gpt-") || chat.getModel().matches("^o\\d.*")) {
      log.info("Sending message to OpenAI");
      return openAIService.getAssistantMessage(messages, chat);
    } else if (chat.getModel().startsWith("gemini-")) {
      log.info("Sending message to Vertex Gemini");
      return vertexGeminiService.getAssistantMessage(messages, chat);
    } else {
      throw new UnsupportedOperationException("Unsupported model for messages: " + chat.getModel());
    }
  }

  public String generateTitle(Chat chat, String userMessage, String assistantMessage) {
    if (chat.getModel().startsWith("gpt-") || chat.getModel().matches("^o\\d.*")) {
      return openAIService.generateTitle(userMessage, assistantMessage);
    } else if (chat.getModel().startsWith("gemini-")) {
      return vertexGeminiService.generateTitle(userMessage, assistantMessage);
    } else {
      throw new UnsupportedOperationException("Unsupported model for title generation: " + chat.getModel());
    }
  }

}