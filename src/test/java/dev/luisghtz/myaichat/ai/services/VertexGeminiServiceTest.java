package dev.luisghtz.myaichat.ai.services;

import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.utils.MessagesUtil;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.FileNotValidException;
import dev.luisghtz.myaichat.mocks.CallResponseMock;
import dev.luisghtz.myaichat.mocks.ChatClientRequestMock;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VertexGeminiServiceTest {

  @Mock
  private ChatClient mockVertextAIChatClient;

  private VertexGeminiService vertexGeminiService;

  // These are concrete test utility classes, not Mockito mocks.
  private ChatClientRequestMock chatClientRequestMock;
  private CallResponseMock callResponseMock;

  @BeforeEach
  void setUp() {
    vertexGeminiService = new VertexGeminiService(mockVertextAIChatClient);
  }

  private Chat createTestChat() {
    Chat chat = new Chat();
    chat.setModel(AppModels.GEMINI_FLASH_2_0.getKey());
    chat.setMaxOutputTokens((short) 2000);
    chat.setIsWebSearchMode(false);
    return chat;
  }

  @Test
  void sendNewMessage_success() {
    // Arrange
    List<AppMessage> appMessages = List.of(
        AppMessage.builder().role("User").content("Hello Gemini").build());
    Chat chat = createTestChat();

    AssistantMessage assistantMessage = new AssistantMessage("Hello there!");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

    callResponseMock = new CallResponseMock(expectedChatResponse);
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock);
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Hello there!", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  void sendNewMessage_withImage_PNG_success() throws MalformedURLException {
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
    callResponseMock = new CallResponseMock(expectedChatResponse);
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock);
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Image received.", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  void sendNewMessage_withImage_JPG_success() throws MalformedURLException {
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
    callResponseMock = new CallResponseMock(expectedChatResponse);
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock);
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("JPG Image received.", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  void sendNewMessage_withImage_JPEG_success() throws MalformedURLException {
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
    callResponseMock = new CallResponseMock(expectedChatResponse);
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock);
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("JPEG Image received.", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  void sendNewMessage_withImage_GIF_success() throws MalformedURLException {
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
    callResponseMock = new CallResponseMock(expectedChatResponse);
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock);
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("GIF Image received.", actualChatResponse.getResult().getOutput().getText());
  }

  @Test
  void sendNewMessage_withImage_RAW_throwsError() {
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
  void sendNewMessage_withImage_MalformedURL_continuesWithoutMedia() {
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
    callResponseMock = new CallResponseMock(expectedChatResponse);

    final List<Message> capturedMessagesContainer = new ArrayList<>();
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock) {
      @Override
      public ChatClient.ChatClientRequestSpec messages(List<Message> messages) {
        capturedMessagesContainer.clear();
        capturedMessagesContainer.addAll(messages);
        return super.messages(messages); // Call super if it does actual work or returns this
      }
    };
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Processed without image.", actualChatResponse.getResult().getOutput().getText());

    assertFalse(capturedMessagesContainer.isEmpty());
    UserMessage sentUserMessage = (UserMessage) capturedMessagesContainer.stream()
        .filter(m -> m instanceof UserMessage && m.getText().equals(appMessageWithImage.getContent()))
        .findFirst().orElse(null);
    assertNotNull(sentUserMessage);
    assertTrue(sentUserMessage.getMedia().isEmpty(), "Media list should be empty due to MalformedURLException");
  }

  @Test
  void sendNewMessage_withImageAndId_ignoresImage() {
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
    callResponseMock = new CallResponseMock(expectedChatResponse);

    final List<Message> capturedMessagesContainer = new ArrayList<>();
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock) {
      @Override
      public ChatClient.ChatClientRequestSpec messages(List<Message> messages) {
        capturedMessagesContainer.clear();
        capturedMessagesContainer.addAll(messages);
        return super.messages(messages);
      }
    };
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    ChatResponse actualChatResponse = vertexGeminiService.sendNewMessage(appMessages, chat);

    // Assert
    assertNotNull(actualChatResponse);
    assertEquals("Processed text only.", actualChatResponse.getResult().getOutput().getText());

    assertFalse(capturedMessagesContainer.isEmpty());
    UserMessage sentUserMessage = (UserMessage) capturedMessagesContainer.stream()
        .filter(m -> m instanceof UserMessage && m.getText().equals(appMessageWithImageAndId.getContent()))
        .findFirst().orElse(null);
    assertNotNull(sentUserMessage);
    assertTrue(sentUserMessage.getMedia().isEmpty(), "Media should be empty as message ID was present");
  }

  @Test
  void sendNewMessage_withCustomPromptAndParams() {
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
    // Assuming chat.getSystemMessage() is null, so MessagesUtil.addSystemMessage
    // adds nothing.

    AssistantMessage assistantMessage = new AssistantMessage("Understood.");
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
    callResponseMock = new CallResponseMock(expectedChatResponse);

    final List<Message> capturedMessagesContainer = new ArrayList<>();
    chatClientRequestMock = new ChatClientRequestMock(callResponseMock) {
      @Override
      public ChatClient.ChatClientRequestSpec messages(List<Message> messages) {
        capturedMessagesContainer.clear();
        capturedMessagesContainer.addAll(messages);
        return super.messages(messages);
      }
    };
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

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

      vertexGeminiService.sendNewMessage(appMessages, chat);
    }

    // Assert
    assertFalse(capturedMessagesContainer.isEmpty());
    // Expected order: CustomUser, CustomAssistant, AppUser
    assertEquals(4, capturedMessagesContainer.size());
    assertTrue(capturedMessagesContainer.get(1) instanceof UserMessage);
    assertEquals("Initial user context", capturedMessagesContainer.get(1).getText());
    assertTrue(capturedMessagesContainer.get(2) instanceof AssistantMessage);
    assertEquals("Initial assistant response", capturedMessagesContainer.get(2).getText());
    assertTrue(capturedMessagesContainer.get(3) instanceof UserMessage);
    assertEquals("Follow up question.", capturedMessagesContainer.get(3).getText());
  }

  @Test
  void generateTitle_success() {
    // Arrange
    String userMessageContent = "Can you tell me about Large Language Models?";
    String assistantMessageContent = "Certainly, Large Language Models are AI models...";
    String expectedTitle = "LLM Overview";

    // This test assumes CallResponseMock can be built with 'content' for the title
    // and its content() method returns this string.
    // If CallResponseMock.content() provided in attachments always returns null,
    // this test would need `expectedTitle` to be `null` or `CallResponseMock`
    // updated.
    ChatResponse expectedChatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(expectedTitle))));
    callResponseMock = new CallResponseMock(expectedChatResponse);

    final List<Message> capturedMessagesContainer = new ArrayList<>();
    final VertexAiGeminiChatOptions[] capturedOptionsContainer = new VertexAiGeminiChatOptions[1];

    chatClientRequestMock = new ChatClientRequestMock(callResponseMock) {
      @Override
      public ChatClient.ChatClientRequestSpec messages(List<Message> messages) {
        capturedMessagesContainer.clear();
        capturedMessagesContainer.addAll(messages);
        return super.messages(messages);
      }

      @Override
      public ChatClient.ChatClientRequestSpec options(ChatOptions options) {
        if (options instanceof VertexAiGeminiChatOptions) {
          capturedOptionsContainer[0] = (VertexAiGeminiChatOptions) options;
        }
        return super.options(options);
      }
    };
    when(mockVertextAIChatClient.prompt()).thenReturn(chatClientRequestMock);

    // Act
    String actualTitle = vertexGeminiService.generateTitle(userMessageContent, assistantMessageContent);

    // Assert
    assertEquals(expectedTitle, actualTitle);

    assertFalse(capturedMessagesContainer.isEmpty());
    assertEquals(3, capturedMessagesContainer.size());
    assertTrue(capturedMessagesContainer.get(0) instanceof UserMessage);
    assertEquals(userMessageContent, capturedMessagesContainer.get(0).getText());
    assertTrue(capturedMessagesContainer.get(1) instanceof AssistantMessage);
    assertEquals(assistantMessageContent, capturedMessagesContainer.get(1).getText());
    assertTrue(capturedMessagesContainer.get(2) instanceof UserMessage); // The TITLE_PROMPT
    assertTrue(capturedMessagesContainer.get(2).getText().startsWith("Generate a concise title"));

    assertNotNull(capturedOptionsContainer[0]);
    assertEquals(AppModels.GEMINI_FLASH_2_0_LITE.getKey(), capturedOptionsContainer[0].getModel());
    assertEquals(50, capturedOptionsContainer[0].getMaxOutputTokens());
  }
}