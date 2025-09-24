package dev.luisghtz.myaichat.ai.services;

import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.utils.ChatClientToolsUtil;
import dev.luisghtz.myaichat.ai.utils.MessagesUtil;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.FileNotValidException;
import dev.luisghtz.myaichat.mocks.CallResponseMock;
import dev.luisghtz.myaichat.mocks.ChatClientRequestMock;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.content.Media;
import static org.springframework.util.MimeTypeUtils.*;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VertexGeminiServiceTest {

  @Mock
  private ChatClient mockVertextAIChatClient;

  @Mock
  private ChatClientToolsUtil chatClientToolsUtil;

  @InjectMocks
  private VertexGeminiService vertexGeminiService;

  @BeforeEach
  void setUp() {
    // Setup common test data
  }

  private Chat createTestChat() {
    Chat chat = new Chat();
    chat.setModel(AppModels.GEMINI_FLASH_2_0.getKey());
    chat.setMaxOutputTokens((short) 2000);
    chat.setIsWebSearchMode(false);
    chat.setFav(false);
    return chat;
  }

  private Chat createTestChatWithWebSearch() {
    Chat chat = new Chat();
    chat.setModel(AppModels.GEMINI_FLASH_2_0.getKey());
    chat.setMaxOutputTokens((short) 2000);
    chat.setIsWebSearchMode(true); // Enable web search mode
    chat.setFav(false);
    return chat;
  }

  private void setupStreamingMocks(Chat chat, ChatResponse expectedChatResponse) {
    // Mock the streaming response
    ChatClient.StreamResponseSpec streamResponseSpec = mock(ChatClient.StreamResponseSpec.class);
    when(streamResponseSpec.chatResponse()).thenReturn(Flux.just(expectedChatResponse));

    CallResponseMock callResponseMock = new CallResponseMock(expectedChatResponse);
    ChatClientRequestMock chatClientRequestMock = new ChatClientRequestMock(callResponseMock, streamResponseSpec);
    when(chatClientToolsUtil.getChatClientRequestSpec(mockVertextAIChatClient, chat)).thenReturn(chatClientRequestMock);
  }

  @Test
  @DisplayName("sendNewMessage - Should return chat response with assistant message")
  void sendNewMessage_ShouldReturnChatResponseWithAssistantMessage() {
    // Arrange
    List<AppMessage> appMessages = List.of(
        AppMessage.builder().role("User").content("Hello Gemini").build());
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("Hello there!");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

    setupStreamingMocks(chat, expectedChatResponse);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Hello there!", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  @DisplayName("sendNewMessage - Should handle PNG image successfully")
  void sendNewMessage_ShouldHandlePNGImageSuccessfully() throws MalformedURLException {
    // Arrange
    AppMessage appMessageWithImage = AppMessage.builder()
        .role("User")
        .content("Check this PNG image")
        .fileUrl("http://example.com/image.png") // id is null
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImage);
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("Image received.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

      // Assert
      assertNotNull(actualChatResponse);
      assertEquals("Image received.", actualChatResponse.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_PNG, new URI("http://example.com/image.png")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle JPG image successfully")
  void sendNewMessage_ShouldHandleJPGImageSuccessfully() throws MalformedURLException {
    // Arrange
    AppMessage appMessageWithImage = AppMessage.builder()
        .role("User")
        .content("Check this JPG image")
        .fileUrl("http://example.com/image.jpg")
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImage);
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("JPG Image received.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

      // Assert
      assertNotNull(actualChatResponse);
      assertEquals("JPG Image received.", actualChatResponse.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_JPEG, new URI("http://example.com/image.jpg")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle JPEG image successfully")
  void sendNewMessage_ShouldHandleJPEGImageSuccessfully() throws MalformedURLException {
    // Arrange
    AppMessage appMessageWithImage = AppMessage.builder()
        .role("User")
        .content("Check this JPEG image")
        .fileUrl("http://example.com/image.jpeg")
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImage);
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("JPEG Image received.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

      // Assert
      assertNotNull(actualChatResponse);
      assertEquals("JPEG Image received.", actualChatResponse.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_JPEG, new URI("http://example.com/image.jpeg")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should handle GIF image successfully")
  void sendNewMessage_ShouldHandleGIFImageSuccessfully() throws MalformedURLException {
    // Arrange
    AppMessage appMessageWithImage = AppMessage.builder()
        .role("User")
        .content("Check this GIF image")
        .fileUrl("http://example.com/image.gif")
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImage);
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("GIF Image received.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

      // Assert
      assertNotNull(actualChatResponse);
      assertEquals("GIF Image received.", actualChatResponse.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_GIF, new URI("http://example.com/image.gif")));
    }
  }

  @Test
  @DisplayName("sendNewMessage - Should throw FileNotValidException for unsupported file format")
  void sendNewMessage_ShouldThrowFileNotValidExceptionForUnsupportedFileFormat() {
    // Arrange
    AppMessage appMessageWithImage = AppMessage.builder()
        .role("User")
        .content("Check this RAW image")
        .fileUrl("http://example.com/image.raw") // Invalid extension
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImage);
    Chat chat = createTestChat();

    // Act & Assert
    FileNotValidException exception = assertThrows(FileNotValidException.class, () -> {
      vertexGeminiService.sendNewMessage(appMessages, chat);
    });
    assertEquals("File not valid. Supported formats: gif, png, jpg, jpeg.", exception.getMessage());
  }

  @Test
  @DisplayName("sendNewMessage - Should handle malformed URL gracefully")
  void sendNewMessage_ShouldHandleMalformedURLGracefully() {
    // Arrange
    AppMessage appMessageWithImage = AppMessage.builder()
        .role("User")
        .content("Check this image with bad URL")
        .fileUrl("htp:/malformed-url.png") // Malformed URL
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImage);
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("Processed without image.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Processed without image.", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  @DisplayName("sendNewMessage - Should ignore image when message has ID")
  void sendNewMessage_ShouldIgnoreImageWhenMessageHasID() {
    // Arrange
    UUID id = UUID.randomUUID();
    AppMessage appMessageWithImageAndId = AppMessage.builder()
        .id(id) // ID present
        .role("User")
        .content("This message has an image URL but also an ID")
        .fileUrl("http://example.com/image.png")
        .build();
    List<AppMessage> appMessages = List.of(appMessageWithImageAndId);
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("Processed text only.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Processed text only.", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  @DisplayName("sendNewMessage - Should handle custom prompt and parameters")
  void sendNewMessage_ShouldHandleCustomPromptAndParameters() {
    // Arrange
    List<AppMessage> appMessages = List.of(
        AppMessage.builder().role("User").content("Follow up question.").build());

    Chat chat = createTestChat();
    CustomPrompt customPrompt = new CustomPrompt();
    customPrompt.setContent("Custom prompt content");
    List<PromptMessage> promptMessages = new ArrayList<>();
    promptMessages.add(PromptMessage.builder().role("User").content("Initial user context").build());
    promptMessages.add(PromptMessage.builder().role("Assistant").content("Initial assistant response").build());
    customPrompt.setMessages(promptMessages);
    chat.setCustomPrompt(customPrompt);

    AssistantMessage assistantMessage = new AssistantMessage("Understood.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Act
    try (MockedStatic<MessagesUtil> messagesUtilMock = mockStatic(MessagesUtil.class)) {
      messagesUtilMock.when(() -> MessagesUtil.addSystemMessage(any(), any())).thenAnswer(invocation -> {
        List<Message> messages = invocation.getArgument(1);
        messages.add(new SystemMessage("Custom prompt content"));
        return null;
      });

      messagesUtilMock.when(() -> MessagesUtil.addInitialMessagesIfApply(any(), any())).thenAnswer(invocation -> {
        List<Message> messages = invocation.getArgument(1);
        for (PromptMessage promptMessage : chat.getCustomPrompt().getMessages()) {
          if (promptMessage.getRole().equals("User")) {
            messages.add(new UserMessage(promptMessage.getContent()));
          } else if (promptMessage.getRole().equals("Assistant")) {
            messages.add(new AssistantMessage(promptMessage.getContent()));
          }
        }
        return null;
      });

      ChatResponse response = vertexGeminiService.sendNewMessage(appMessages, chat);
      
      // Assert
      assertNotNull(response);
      assertEquals("Understood.", response.getResult().getOutput().getText());
    }
  }

  @Test
  @DisplayName("generateTitle - Should return generated title successfully")
  void generateTitle_ShouldReturnGeneratedTitleSuccessfully() {
    // Arrange
    String userMessageContent = "Can you tell me about Large Language Models?";
    String assistantMessageContent = "Certainly, Large Language Models are AI models...";
    String expectedTitle = "LLM Overview";

    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(expectedTitle))));
    CallResponseMock callResponseMock = new CallResponseMock(expectedChatResponse);
    ChatClientRequestMock chatClientRequestMock = new ChatClientRequestMock(callResponseMock);
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    String actualTitle = vertexGeminiService.generateTitle(userMessageContent, assistantMessageContent);

    // Assert
    assertEquals(expectedTitle, actualTitle);
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
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Act
    ChatResponse response = vertexGeminiService.sendNewMessage(messages, chat);

    // Assert
    assertNotNull(response);
    assertEquals("I'll search for current information.", response.getResult().getOutput().getText());
    verify(chatClientToolsUtil).getChatClientRequestSpec(mockVertextAIChatClient, chat);
  }

  @Test
  @DisplayName("sendNewMessage - Should handle PNG image with web search mode enabled")
  void sendNewMessage_ShouldHandlePNGImageWithWebSearchModeEnabled() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Analyze this image and search for related information")
        .fileUrl("http://example.com/image.png")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChatWithWebSearch();
    var assistantMessage = new AssistantMessage("Image analyzed with web search capabilities.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Spy on Media constructor to verify it was called
    try (var mediaMockedConstruction = mockStatic(Media.class, CALLS_REAL_METHODS)) {
      // Act
      ChatResponse response = vertexGeminiService.sendNewMessage(messages, chat);

      // Assert
      assertNotNull(response);
      assertEquals("Image analyzed with web search capabilities.", response.getResult().getOutput().getText());
      mediaMockedConstruction.verify(() -> new Media(IMAGE_PNG, new URI("http://example.com/image.png")));
      verify(chatClientToolsUtil).getChatClientRequestSpec(mockVertextAIChatClient, chat);
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
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    setupStreamingMocks(chat, expectedChatResponse);

    // Act
    ChatResponse response = vertexGeminiService.sendNewMessage(messages, chat);

    // Assert
    assertNotNull(response);
    assertEquals("Continuing with web search capabilities.", response.getResult().getOutput().getText());
    verify(chatClientToolsUtil).getChatClientRequestSpec(mockVertextAIChatClient, chat);
  }

  @Test
  @DisplayName("sendNewMessage - Should throw FileNotValidException for unsupported file format with web search mode")
  void sendNewMessage_ShouldThrowFileNotValidExceptionForUnsupportedFileFormatWithWebSearchMode() throws MalformedURLException {
    // Arrange
    List<AppMessage> messages = new ArrayList<>();
    AppMessage appMessage = AppMessage.builder()
        .role("User")
        .content("Check this image with web search")
        .fileUrl("http://example.com/image.raw")
        .build();
    messages.add(appMessage);
    Chat chat = createTestChatWithWebSearch();
    
    // Act & Assert
    assertThrows(FileNotValidException.class, () -> {
      vertexGeminiService.sendNewMessage(messages, chat);
    });
  }
}