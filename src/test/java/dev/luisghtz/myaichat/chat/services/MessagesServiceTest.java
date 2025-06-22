package dev.luisghtz.myaichat.chat.services;

import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.TokensSum;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;
import dev.luisghtz.myaichat.chat.utils.MessagesUtils;
import dev.luisghtz.myaichat.image.providers.AwsS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessagesServiceTest {

  @Mock
  private AIService aiProviderService;
  @Mock
  private ChatService chatService;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private AwsS3Service awsS3Service;

  @InjectMocks
  private MessagesService messagesService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    // Set the private field 'cdn' using reflection
    try {
      java.lang.reflect.Field cdnField = MessagesService.class.getDeclaredField("cdn");
      cdnField.setAccessible(true);
      cdnField.set(messagesService, "https://cdn.example.com/");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testGetPreviousMessages_ReturnsHistoryChatDto() {
    UUID chatId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    Chat chat = mock(Chat.class);
    TokensSum tokensSum = new TokensSum(5L, 10L);
    List<AppMessage> messages = List.of(mock(AppMessage.class));

    when(chatService.findChatById(chatId)).thenReturn(chat);
    when(messageRepository.getSumOfPromptAndCompletionTokensByChatId(chatId)).thenReturn(tokensSum);
    when(messageRepository.findAllByChatOrderByCreatedAtDesc(chat, pageable)).thenReturn(messages);
    when(chat.getModel()).thenReturn("gpt-3");
    Short maxOutputTokens = 100;
    when(chat.getMaxOutputTokens()).thenReturn(maxOutputTokens);

    HistoryChatDto result = messagesService.getPreviousMessages(chatId, pageable);

    assertThat(result).isNotNull();
    assertThat(result.getModel()).isEqualTo("gpt-3");
    assertThat(result.getTotalPromptTokens()).isEqualTo(5);
    assertThat(result.getTotalCompletionTokens()).isEqualTo(10);
  }

  @Test
  void testSendNewMessage_NewChat() {
    NewMessageRequestDto requestDto = mock(NewMessageRequestDto.class);
    String fileUrl = "file.png";
    Chat chat = mock(Chat.class);
    AppMessage userMessage = mock(AppMessage.class);
    AppMessage assistantMessage = mock(AppMessage.class);
    TokensSum tokensSum = new TokensSum(1L, 2L);
    when(chatService.getChat(requestDto)).thenReturn(chat);
    when(chat.getMessages()).thenReturn(Collections.emptyList());
    try (MockedStatic<MessagesUtils> utils = Mockito.mockStatic(MessagesUtils.class)) {
      utils.when(() -> MessagesUtils.processUserMessage(requestDto, chat, fileUrl)).thenReturn(userMessage);
      utils.when(() -> MessagesUtils.createAssistantMessage(any(), eq(chat))).thenReturn(assistantMessage);
      var assistantMessageRes = new AssistantMessage("Hello");
      ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessageRes)));
      when(aiProviderService.sendNewMessage(anyList(), eq(chat))).thenReturn(mockResponse);
      when(messageRepository.saveAll(any())).thenReturn(List.of(userMessage, assistantMessage));
      when(messageRepository.getSumOfPromptAndCompletionTokensByChatId(any())).thenReturn(tokensSum);

      doNothing().when(chatService).generateAndSetTitleForNewChat(eq(chat), eq(requestDto), any());
      when(chat.getId()).thenReturn(UUID.randomUUID());
      when(chat.getTitle()).thenReturn("Chat Title");

      AssistantMessageResponseDto result = messagesService.sendNewMessage(requestDto, fileUrl);

      assertThat(result).isNotNull();
      assertThat(result.getTotalChatPromptTokens()).isEqualTo(1);
      assertThat(result.getTotalChatCompletionTokens()).isEqualTo(2);
    }
  }

  @Test
  void testSaveAll_DelegatesToRepository() {
    AppMessage msg1 = mock(AppMessage.class);
    AppMessage msg2 = mock(AppMessage.class);
    List<AppMessage> messages = List.of(msg1, msg2);

    when(messageRepository.saveAll(messages)).thenReturn(messages);

    List<AppMessage> result = messagesService.saveAll(messages);

    assertThat(result).containsExactly(msg1, msg2);
    verify(messageRepository).saveAll(messages);
  }

  @Test
  void testDeleteAllByChat_DeletesMessagesAndImages() {
    UUID chatId = UUID.randomUUID();
    AppMessage msg1 = mock(AppMessage.class);
    AppMessage msg2 = mock(AppMessage.class);

    when(messageRepository.findAllByChatId(chatId)).thenReturn(List.of(msg1, msg2));
    when(msg1.getImageUrl()).thenReturn("https://cdn.example.com/image1.png");
    when(msg2.getImageUrl()).thenReturn(null);

    doNothing().when(awsS3Service).deleteFile("image1.png");
    doNothing().when(messageRepository).deleteAllByChatId(chatId);

    messagesService.deleteAllByChat(chatId);

    verify(awsS3Service).deleteFile("image1.png");
    verify(messageRepository).deleteAllByChatId(chatId);
  }

  @Test
  void testIsChatNew_ReturnsTrueIfMessagesNullOrEmpty() {
    Chat chat = mock(Chat.class);
    when(chat.getMessages()).thenReturn(null);
    assertThat(invokeIsChatNew(chat)).isTrue();

    when(chat.getMessages()).thenReturn(Collections.emptyList());
    assertThat(invokeIsChatNew(chat)).isTrue();
  }

  @Test
  void testIsChatNew_ReturnsFalseIfMessagesNotEmpty() {
    Chat chat = mock(Chat.class);
    when(chat.getMessages()).thenReturn(List.of(mock(AppMessage.class)));
    assertThat(invokeIsChatNew(chat)).isFalse();
  }

  // Helper to invoke private method
  private boolean invokeIsChatNew(Chat chat) {
    try {
      var method = MessagesService.class.getDeclaredMethod("isChatNew", Chat.class);
      method.setAccessible(true);
      return (boolean) method.invoke(messagesService, chat);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
