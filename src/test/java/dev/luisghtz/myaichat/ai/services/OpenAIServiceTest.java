package dev.luisghtz.myaichat.ai.services;

import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.models.CallResponseMock;
import dev.luisghtz.myaichat.ai.models.ChatClientRequestMock;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.ImageNotValidException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.Media;
import static org.springframework.util.MimeTypeUtils.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceTest {

  @Mock
  private ChatClient openAIChatClient;

  private OpenAIService openAIService;

  @BeforeEach
  void setUp() {
    openAIService = new OpenAIService(openAIChatClient);
  }

  @Test
  void sendNewMessage_success() {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    messages.add(AppMessage.builder()
        .role("Assistant")
        .content("Hello")
        .build());
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(openAIChatClient.prompt()).thenReturn(mockRequest);

    // Act
    ChatResponse response = openAIService.sendNewMessage(messages, chat);

    // Assert
    assertNotNull(response);
    assertEquals("Hello", response.getResult().getOutput().getText());
  }

  @Test
  void sendNewMessage_withImage_PNG_success() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .imageUrl("https://example.com/image.png")
        .build();
    messages.add(appMessage);
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(openAIChatClient.prompt()).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_PNG, new URL("https://example.com/image.png")));
    }
  }

  @Test
  void sendNewMessage_withImage_JPG_success() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .imageUrl("https://example.com/image.jpg")
        .build();
    messages.add(appMessage);
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(openAIChatClient.prompt()).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_JPEG, new URL("https://example.com/image.jpg")));
    }
  }

  @Test
  void sendNewMessage_withImage_JPEG_success() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .imageUrl("https://example.com/image.jpeg")
        .build();
    messages.add(appMessage);
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(openAIChatClient.prompt()).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_JPEG, new URL("https://example.com/image.jpeg")));
    }
  }

  @Test
  void sendNewMessage_withImage_GIF_success() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .imageUrl("https://example.com/image.gif")
        .build();
    messages.add(appMessage);
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(openAIChatClient.prompt()).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_GIF, new URL("https://example.com/image.gif")));
    }
  }

  @Test
  void sendNewMessage_withImage_RAW_throwsError() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .imageUrl("https://example.com/image.raw")
        .build();
    messages.add(appMessage);
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    // Act
    assertThrows(ImageNotValidException.class, () -> {
      // Assert
      openAIService.sendNewMessage(messages, chat);
    });
  }

  @Test
  void generateTitle_success() {
    // Arrange
    String userMessage = "What is the weather today?";
    String assistantMessage = "The weather is sunny.";
    var titleResponse = new AssistantMessage("Sunny Weather");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(titleResponse)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(openAIChatClient.prompt()).thenReturn(mockRequest);

    // Act
    String response = openAIService.generateTitle(userMessage, assistantMessage);

    // Assert
    assertEquals(titleResponse.getText(), response);
  }
}