package dev.luisghtz.myaichat.chat.utils;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class MessagesutilsTest {

  @Test
  void processUserMessage_shouldCreateAppMessageWithUserRoleAndContent() {
    NewMessageRequestDto dto = Mockito.mock(NewMessageRequestDto.class);
    Mockito.when(dto.getPrompt()).thenReturn("Hello, AI!");
    Chat chat = new Chat();
    String fileUrl = null;

    AppMessage message = MessagesUtils.processUserMessage(dto, chat, fileUrl);

    assertEquals("User", message.getRole());
    assertEquals("Hello, AI!", message.getContent());
    assertEquals(chat, message.getChat());
    assertNotNull(message.getCreatedAt());
    assertNull(message.getFileUrl());
  }

  @Test
  void processUserMessage_shouldSetFileUrlIfProvided() {
    NewMessageRequestDto dto = Mockito.mock(NewMessageRequestDto.class);
    Mockito.when(dto.getPrompt()).thenReturn("Prompt with image");
    Chat chat = new Chat();
    String fileUrl = "http://example.com/image.png";

    AppMessage message = MessagesUtils.processUserMessage(dto, chat, fileUrl);

    assertEquals(fileUrl, message.getFileUrl());
  }

  @Test
  void createAssistantMessage_shouldCreateAppMessageWithAssistantRoleAndTokens() {
    Chat chat = new Chat();
    Usage usage = Mockito.mock(Usage.class);
    Mockito.when(usage.getPromptTokens()).thenReturn(5);
    Mockito.when(usage.getCompletionTokens()).thenReturn(10);
    Mockito.when(usage.getTotalTokens()).thenReturn(15);
    ChatResponseMetadata metadata = Mockito.mock(ChatResponseMetadata.class);
    AssistantMessage assistantMessage = new AssistantMessage("AI response");
    var chatResponse = Mockito.mock(ChatResponse.class);
    List<Generation> generations = List.of(new Generation(assistantMessage));
    Mockito.when(chatResponse.getMetadata()).thenReturn(metadata);
    Mockito.when(metadata.getUsage()).thenReturn(usage);
    Mockito.when(chatResponse.getResult()).thenReturn(generations.get(0));

    var res = MessagesUtils.createAssistantMessage(chatResponse, chat);

    assertEquals("Assistant", res.getRole());
    assertEquals("AI response", res.getContent());
    assertEquals(5, res.getPromptTokens());
    assertEquals(10, res.getCompletionTokens());
    assertEquals(15, res.getTotalTokens());
    assertEquals(chat, res.getChat());
    assertNotNull(res.getCreatedAt());
  }

  @Test
  void processUserMessage_shouldNotSetFileUrlIfEmpty() {
    NewMessageRequestDto dto = Mockito.mock(NewMessageRequestDto.class);
    Mockito.when(dto.getPrompt()).thenReturn("Prompt");
    Chat chat = new Chat();
    String fileUrl = "";

    AppMessage message = MessagesUtils.processUserMessage(dto, chat, fileUrl);

    assertNull(message.getFileUrl());
  }
}
