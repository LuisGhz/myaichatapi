package dev.luisghtz.myaichat.chat.services;

import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.TokensSum;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;
import dev.luisghtz.myaichat.chat.utils.MessagesUtils;
import dev.luisghtz.myaichat.file.providers.AwsS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  @Mock
  private ChatRepository chatRepository;

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

  private UserJwtDataDto createUserJwtData(String userId) {
    UserJwtDataDto user = new UserJwtDataDto();
    user.setId(userId);
    return user;
  }

  @Test
  void testGetPreviousMessages_ReturnsHistoryChatDto() {
    UUID chatId = UUID.randomUUID();
    var userId = UUID.randomUUID();
    var userJwt = createUserJwtData(userId.toString());
    var userChat = new User();
    userChat.setId(userId);
    when(chatService.findChatById(chatId)).thenReturn(mock(Chat.class));
    when(messageRepository.getSumOfPromptAndCompletionTokensByChatId(chatId))
        .thenReturn(new TokensSum(5L, 10L));
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
    when(chat.getUser()).thenReturn(userChat);

    HistoryChatDto result = messagesService.getPreviousMessages(chatId, pageable, userJwt);

    assertThat(result).isNotNull();
    assertThat(result.getModel()).isEqualTo("gpt-3");
    assertThat(result.getTotalPromptTokens()).isEqualTo(5);
    assertThat(result.getTotalCompletionTokens()).isEqualTo(10);
  }

  @Test
  void testGetPreviousMessages_ShouldThrowException_WhenUserIdIsDifferentFromChatUserId() {
    UUID chatId = UUID.randomUUID();
    var userId = UUID.randomUUID();
    var userJwt = createUserJwtData(userId.toString());
    var userChat = new User();
    Chat chat = mock(Chat.class);
    Pageable pageable = PageRequest.of(0, 10);
    userChat.setId(UUID.randomUUID());
    when(chatService.findChatById(chatId)).thenReturn(chat);
    when(chat.getUser()).thenReturn(userChat);

    assertThrows(ResponseStatusException.class, () -> messagesService.getPreviousMessages(chatId, pageable, userJwt));
  }

  @Test
  void testSendNewMessage_NewChat() {
    NewMessageRequestDto requestDto = mock(NewMessageRequestDto.class);
    String fileUrl = "file.png";
    Chat chat = mock(Chat.class);
    AppMessage userMessage = mock(AppMessage.class);
    AppMessage assistantMessage = mock(AppMessage.class);
    TokensSum tokensSum = new TokensSum(1L, 2L);
    var userJwt = createUserJwtData(UUID.randomUUID().toString());
    when(requestDto.getChatId()).thenReturn(null);
    when(chatService.getChat(requestDto)).thenReturn(chat);
    when(chat.getMessages()).thenReturn(Collections.emptyList());
    when(chat.getId()).thenReturn(UUID.randomUUID());
    when(chat.getTitle()).thenReturn("New Chat");
    when(chat.getModel()).thenReturn("gpt-3");
    when(chat.getMaxOutputTokens()).thenReturn((short) 100);
    when(chat.getIsWebSearchMode()).thenReturn(false);
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

      AssistantMessageResponseDto result = messagesService.sendNewMessage(requestDto, fileUrl, userJwt);

      assertThat(result).isNotNull();
      assertThat(result.getTotalChatPromptTokens()).isEqualTo(1);
      assertThat(result.getTotalChatCompletionTokens()).isEqualTo(2);
    }
  }

  @Test
  void testSendNewMessage_ShouldThrowException_WhenUserIdIsDifferentFromChatUserId() throws Exception {
    NewMessageRequestDto requestDto = mock(NewMessageRequestDto.class);
    Chat chat = mock(Chat.class);
    var user = new User();
    user.setId(UUID.randomUUID());
    var userJwt = createUserJwtData(UUID.randomUUID().toString());
    var messages = List.of(
        new AppMessage(UUID.randomUUID(), "user", "Hello", null, null, null, null, null, null),
        new AppMessage(UUID.randomUUID(), "assistant", "Hi there!", null, null, null, null, null, null));
    when(requestDto.getChatId()).thenReturn(null);
    when(chatService.getChat(requestDto)).thenReturn(chat);
    when(chat.getMessages()).thenReturn(messages);
    when(chat.getId()).thenReturn(UUID.randomUUID());
    when(chat.getTitle()).thenReturn("New Chat");
    when(chat.getModel()).thenReturn("gpt-3");
    when(chat.getMaxOutputTokens()).thenReturn((short) 100);
    when(chat.getIsWebSearchMode()).thenReturn(false);
    when(chat.getUser()).thenReturn(user);
    when(chatService.getChat(requestDto)).thenReturn(chat);
    when(chatRepository.findById(any(UUID.class))).thenReturn(Optional.of(chat));
    assertThrows(ResponseStatusException.class, () -> messagesService.sendNewMessage(requestDto, null, userJwt));
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
  void testDeleteAllByChat_DeletesMessagesAndFiles() {
    UUID chatId = UUID.randomUUID();
    AppMessage msg1 = mock(AppMessage.class);
    AppMessage msg2 = mock(AppMessage.class);

    when(messageRepository.findAllByChatId(chatId)).thenReturn(List.of(msg1, msg2));
    when(msg1.getFileUrl()).thenReturn("https://cdn.example.com/image1.png");
    when(msg2.getFileUrl()).thenReturn(null);

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
