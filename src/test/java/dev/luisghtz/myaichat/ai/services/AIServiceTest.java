package dev.luisghtz.myaichat.ai.services;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

  @Mock
  private OpenAIService openAIService;

  @Mock
  private VertexGeminiService vertexGeminiService;

  @InjectMocks
  private AIService aiService;

  private Chat chat;
  private List<AppMessage> messages;

  @BeforeEach
  void setUp() {
    chat = new Chat();
    messages = Collections.singletonList(new AppMessage());
  }

  @Test
  void sendNewMessage_withGptModel_shouldCallOpenAIService() {
    chat.setModel("gpt-4");
    ChatResponse expectedResponse = mock(ChatResponse.class);
    when(openAIService.sendNewMessage(messages, chat)).thenReturn(expectedResponse);

    ChatResponse actualResponse = aiService.sendNewMessage(messages, chat);

    assertEquals(expectedResponse, actualResponse);
    verify(openAIService, times(1)).sendNewMessage(messages, chat);
    verify(vertexGeminiService, never()).sendNewMessage(any(), any());
  }

  @Test
  void sendNewMessage_withGeminiModel_shouldCallVertexGeminiService() {
    chat.setModel("gemini-pro");
    ChatResponse expectedResponse = mock(ChatResponse.class);
    when(vertexGeminiService.sendNewMessage(messages, chat)).thenReturn(expectedResponse);

    ChatResponse actualResponse = aiService.sendNewMessage(messages, chat);

    assertEquals(expectedResponse, actualResponse);
    verify(vertexGeminiService, times(1)).sendNewMessage(messages, chat);
    verify(openAIService, never()).sendNewMessage(any(), any());
  }

  @Test
  void sendNewMessage_withUnsupportedModel_shouldThrowException() {
    chat.setModel("unsupported-model");

    assertThrows(UnsupportedOperationException.class, () -> {
      aiService.sendNewMessage(messages, chat);
    });

    verify(openAIService, never()).sendNewMessage(any(), any());
    verify(vertexGeminiService, never()).sendNewMessage(any(), any());
  }

  @Test
  void generateTitle_withGptModel_shouldCallOpenAIService() {
    chat.setModel("gpt-4");
    String userMessage = "Hello";
    String assistantMessage = "Hi there";
    String expectedTitle = "GPT Title";
    when(openAIService.generateTitle(userMessage, assistantMessage)).thenReturn(expectedTitle);

    String actualTitle = aiService.generateTitle(chat, userMessage, assistantMessage);

    assertEquals(expectedTitle, actualTitle);
    verify(openAIService, times(1)).generateTitle(userMessage, assistantMessage);
    verify(vertexGeminiService, never()).generateTitle(any(), any());
  }

  @Test
  void generateTitle_withGeminiModel_shouldCallVertexGeminiService() {
    chat.setModel("gemini-pro");
    String userMessage = "Hello";
    String assistantMessage = "Hi there";
    String expectedTitle = "Gemini Title";
    when(vertexGeminiService.generateTitle(userMessage, assistantMessage)).thenReturn(expectedTitle);

    String actualTitle = aiService.generateTitle(chat, userMessage, assistantMessage);

    assertEquals(expectedTitle, actualTitle);
    verify(vertexGeminiService, times(1)).generateTitle(userMessage, assistantMessage);
    verify(openAIService, never()).generateTitle(any(), any());
  }

  @Test
  void generateTitle_withUnsupportedModel_shouldThrowException() {
    chat.setModel("unsupported-model");
    String userMessage = "Hello";
    String assistantMessage = "Hi there";

    assertThrows(UnsupportedOperationException.class, () -> {
      aiService.generateTitle(chat, userMessage, assistantMessage);
    });

    verify(openAIService, never()).generateTitle(any(), any());
    verify(vertexGeminiService, never()).generateTitle(any(), any());
  }
}
