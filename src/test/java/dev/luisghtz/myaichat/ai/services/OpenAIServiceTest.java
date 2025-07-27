package dev.luisghtz.myaichat.ai.services;

import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.utils.ChatClientToolsUtil;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.FileNotValidException;
import dev.luisghtz.myaichat.mocks.CallResponseMock;
import dev.luisghtz.myaichat.mocks.ChatClientRequestMock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.content.Media;
import static org.springframework.util.MimeTypeUtils.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceTest {

  @Mock
  private ChatClient openAIChatClient;

  @Mock
  private ChatClientToolsUtil chatClientToolsUtil;

  @InjectMocks
  private OpenAIService openAIService;

  @BeforeEach
  void setUp() {
    // Setup common test data
  }

  private Chat createTestChat() {
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    chat.setMaxOutputTokens((short) 2000);
    chat.setIsWebSearchMode(false); // Set to avoid NPE in MessagesUtil
    chat.setFav(false);
    return chat;
  }

  private Chat createTestChatWithWebSearch() {
    Chat chat = new Chat();
    chat.setModel(AppModels.GPT_4O_MINI.getKey());
    chat.setMaxOutputTokens((short) 2000);
    chat.setIsWebSearchMode(true); // Enable web search mode
    chat.setFav(false);
    return chat;
  }

  @Test
  @DisplayName("sendNewMessage - Should return chat response with assistant message")
  void sendNewMessage_ShouldReturnChatResponseWithAssistantMessage() {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    messages.add(AppMessage.builder()
        .role("Assistant")
        .content("Hello")
        .build());
    Chat chat = createTestChat();
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Act
    ChatResponse response = openAIService.sendNewMessage(messages, chat);

    // Assert
    assertNotNull(response);
    assertEquals("Hello", response.getResult().getOutput().getText());
  }

  @Test
  @DisplayName("sendNewMessage - Should handle PNG image successfully")
  void sendNewMessage_ShouldHandlePNGImageSuccessfully() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .fileUrl("https://example.com/image.png")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChat();
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_PNG, new URI("https://example.com/image.png")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle JPG image successfully")
  void sendNewMessage_ShouldHandleJPGImageSuccessfully() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .fileUrl("https://example.com/image.jpg")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChat();
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_JPEG, new URI("https://example.com/image.jpg")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle JPEG image successfully")
  void sendNewMessage_ShouldHandleJPEGImageSuccessfully() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .fileUrl("https://example.com/image.jpeg")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChat();
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_JPEG, new URI("https://example.com/image.jpeg")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle GIF image successfully")
  void sendNewMessage_ShouldHandleGIFImageSuccessfully() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .fileUrl("https://example.com/image.gif")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChat();
    var assistantMessage = new AssistantMessage("Hello");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Hello", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_GIF, new URI("https://example.com/image.gif")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should throw FileNotValidException for unsupported file format")
  void sendNewMessage_ShouldThrowFileNotValidExceptionForUnsupportedFileFormat() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image")
        .fileUrl("https://example.com/image.raw")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChat();
    
    // Act & Assert
    assertThrows(FileNotValidException.class, () -> {
      openAIService.sendNewMessage(messages, chat);
    });
  }

  @Test
  @DisplayName("generateTitle - Should return generated title successfully")
  void generateTitle_ShouldReturnGeneratedTitleSuccessfully() {
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

  @Test
  @DisplayName("sendNewMessage - Should return chat response with web search mode enabled")
  void sendNewMessage_ShouldReturnChatResponseWithWebSearchModeEnabled() {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    messages.add(AppMessage.builder()
        .role("User")
        .content("What is the current president of USA?")
        .build());
    Chat chat = createTestChatWithWebSearch();
    var assistantMessage = new AssistantMessage("I'll search for current information.");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Act
    ChatResponse response = openAIService.sendNewMessage(messages, chat);

    // Assert
    assertNotNull(response);
    assertEquals("I'll search for current information.", response.getResult().getOutput().getText());
    verify(chatClientToolsUtil).getChatClientRequestSpec(openAIChatClient, chat);
  }

  @Test
  @DisplayName("sendNewMessage - Should handle PNG image with web search mode enabled")
  void sendNewMessage_ShouldHandlePNGImageWithWebSearchModeEnabled() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Analyze this image and search for related information")
        .fileUrl("https://example.com/image.png")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChatWithWebSearch();
    var assistantMessage = new AssistantMessage("Image analyzed with web search capabilities.");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = openAIService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Image analyzed with web search capabilities.", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_PNG, new URI("https://example.com/image.png")));
      verify(chatClientToolsUtil).getChatClientRequestSpec(openAIChatClient, chat);
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle assistant message with web search mode enabled")
  void sendNewMessage_ShouldHandleAssistantMessageWithWebSearchModeEnabled() {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    messages.add(AppMessage.builder()
        .role("Assistant")
        .content("Previous response with web search")
        .build());
    messages.add(AppMessage.builder()
        .role("User")
        .content("Continue the search")
        .build());
    Chat chat = createTestChatWithWebSearch();
    var assistantMessage = new AssistantMessage("Continuing with web search capabilities.");
    ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    CallResponseMock callResponse = new CallResponseMock(mockResponse);
    ChatClientRequestMock mockRequest = new ChatClientRequestMock(callResponse);
    when(chatClientToolsUtil.getChatClientRequestSpec(openAIChatClient, chat)).thenReturn(mockRequest);

    // Act
    ChatResponse response = openAIService.sendNewMessage(messages, chat);

    // Assert
    assertNotNull(response);
    assertEquals("Continuing with web search capabilities.", response.getResult().getOutput().getText());
    verify(chatClientToolsUtil).getChatClientRequestSpec(openAIChatClient, chat);
  }

  @Test
  @DisplayName("sendNewMessage - Should throw FileNotValidException for unsupported file format with web search mode")
  void sendNewMessage_ShouldThrowFileNotValidExceptionForUnsupportedFileFormatWithWebSearchMode() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image with web search")
        .fileUrl("https://example.com/image.raw")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChatWithWebSearch();
    
    // Act & Assert
    assertThrows(FileNotValidException.class, () -> {
      openAIService.sendNewMessage(messages, chat);
    });
  }
}