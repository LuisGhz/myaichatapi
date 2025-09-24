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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

  @Nested
  @DisplayName("GetPreviousMessages Method")
  class GetPreviousMessagesTests {

    @Test
    @DisplayName("getPreviousMessages - Should return HistoryChatDto with correct data")
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
      when(chat.getIsWebSearchMode()).thenReturn(true);

      HistoryChatDto result = messagesService.getPreviousMessages(chatId, pageable, userJwt);

      assertThat(result).isNotNull();
      assertThat(result.getModel()).isEqualTo("gpt-3");
      assertThat(result.getTotalPromptTokens()).isEqualTo(5);
      assertThat(result.getTotalCompletionTokens()).isEqualTo(10);
      assertThat(result.getMaxOutputTokens()).isEqualTo(maxOutputTokens);
      assertThat(result.getIsWebSearchMode()).isEqualTo(true);
    }

    @Test
    @DisplayName("getPreviousMessages - Should throw exception when user ID differs from chat user ID")
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
  }

  @Nested
  @DisplayName("SendNewMessage Method")
  class SendNewMessageTests {

    @Test
    @DisplayName("sendNewMessage - Should handle new chat creation successfully")
    void testSendNewMessage_NewChat() {
      NewMessageRequestDto requestDto = new NewMessageRequestDto();
      requestDto.setChatId(null);
      String fileUrl = "file.png";
      Chat chat = new Chat();
      chat.setId(UUID.randomUUID());
      chat.setTitle(null); // Set to null for new chat testing
      chat.setModel("gpt-3");
      chat.setMaxOutputTokens((short) 100);
      chat.setIsWebSearchMode(false);
      User user = new User();
      user.setId(UUID.randomUUID());
      chat.setUser(user);

      AppMessage userMessage = mock(AppMessage.class);
      when(userMessage.getRole()).thenReturn("User");
      when(userMessage.getContent()).thenReturn("Test message");
      
      // Set the messages list on the real Chat entity
      chat.setMessages(List.of(userMessage));
      
      AppMessage assistantMessage = mock(AppMessage.class);
      TokensSum tokensSum = new TokensSum(1L, 2L);
      var userJwt = createUserJwtData(user.getId().toString());

      // Only stub what's needed
      when(chatService.getChat(requestDto, userJwt.getId())).thenReturn(chat);
      when(chatService.findChatById(chat.getId())).thenReturn(chat);
      when(messageRepository.getSumOfPromptAndCompletionTokensByChatId(chat.getId())).thenReturn(tokensSum);

      try (MockedStatic<MessagesUtils> utils = Mockito.mockStatic(MessagesUtils.class)) {
        utils.when(() -> MessagesUtils.processUserMessage(requestDto, chat, fileUrl)).thenReturn(userMessage);
        utils.when(() -> MessagesUtils.createAssistantMessage(any(), eq(chat))).thenReturn(assistantMessage);

        var assistantMessageRes = new AssistantMessage("Hello");
        Generation generation = new Generation(assistantMessageRes);
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockResponse.getResult()).thenReturn(generation);
        
        // Mock the metadata properly with usage information
        org.springframework.ai.chat.metadata.ChatResponseMetadata metadata = mock(org.springframework.ai.chat.metadata.ChatResponseMetadata.class);
        org.springframework.ai.chat.metadata.Usage usage = mock(org.springframework.ai.chat.metadata.Usage.class);
        when(usage.getTotalTokens()).thenReturn(10);
        when(usage.getPromptTokens()).thenReturn(5);
        when(usage.getCompletionTokens()).thenReturn(5);
        when(metadata.getUsage()).thenReturn(usage);
        when(mockResponse.getMetadata()).thenReturn(metadata);
        
        when(aiProviderService.getAssistantMessage(anyList(), eq(chat))).thenReturn(Flux.just(mockResponse));
        when(aiProviderService.generateTitle(eq(chat), anyString(), anyString())).thenReturn("Generated Title");
        doNothing().when(chatService).updateChatTitle(any(), anyString());

        AssistantMessageResponseDto result = messagesService.getAssistantMessage(requestDto, fileUrl, userJwt);

        assertThat(result).isNotNull();
        assertThat(result.getTotalChatPromptTokens()).isEqualTo(1);
        assertThat(result.getTotalChatCompletionTokens()).isEqualTo(2);
      }
    }

    @Test
    @DisplayName("sendNewMessage - Should throw exception when user ID differs from chat user ID")
    void testSendNewMessage_ShouldThrowException_WhenUserIdIsDifferentFromChatUserId() {
      NewMessageRequestDto requestDto = new NewMessageRequestDto();
      requestDto.setChatId(null);

      Chat chat = new Chat();
      chat.setId(UUID.randomUUID());
      chat.setTitle("New Chat");
      chat.setModel("gpt-3");
      chat.setMaxOutputTokens((short) 100);
      chat.setIsWebSearchMode(false);

      User user = new User();
      user.setId(UUID.randomUUID());
      chat.setUser(user);

      List<AppMessage> messages = List.of(
          new AppMessage(UUID.randomUUID(), "user", "Hello", null, null, null, null, null, null),
          new AppMessage(UUID.randomUUID(), "assistant", "Hi there!", null, null, null, null, null, null));
      chat.setMessages(messages);

      var userJwt = createUserJwtData(UUID.randomUUID().toString());

      when(chatService.getChat(requestDto, userJwt.getId())).thenReturn(chat);
      when(chatService.findChatById(chat.getId())).thenReturn(chat);

      assertThrows(ResponseStatusException.class, () -> messagesService.getAssistantMessage(requestDto, null, userJwt));
    }
  }

  @Nested
  @DisplayName("SaveAll Method")
  class SaveAllTests {

    @Test
    @DisplayName("saveAll - Should delegate to repository correctly")
    void testSaveAll_DelegatesToRepository() {
      AppMessage msg1 = mock(AppMessage.class);
      AppMessage msg2 = mock(AppMessage.class);
      List<AppMessage> messages = List.of(msg1, msg2);

      when(messageRepository.saveAll(messages)).thenReturn(messages);

      List<AppMessage> result = messagesService.saveAll(messages);

      assertThat(result).containsExactly(msg1, msg2);
      verify(messageRepository).saveAll(messages);
    }
  }

  @Nested
  @DisplayName("DeleteAllByChat Method")
  class DeleteAllByChatTests {

    @Test
    @DisplayName("deleteAllByChat - Should delete messages and associated files")
    void testDeleteAllByChat_DeletesMessagesAndFiles() {
      UUID chatId = UUID.randomUUID();
      var userId = UUID.randomUUID();
      var userJwt = createUserJwtData(userId.toString());
      var userChat = new User();
      userChat.setId(userId);
      
      Chat chat = mock(Chat.class);
      AppMessage msg1 = mock(AppMessage.class);
      AppMessage msg2 = mock(AppMessage.class);

      when(chatService.findChatById(chatId)).thenReturn(chat);
      when(chat.getUser()).thenReturn(userChat);
      when(messageRepository.findAllByChatId(chatId)).thenReturn(List.of(msg1, msg2));
      when(msg1.getFileUrl()).thenReturn("https://cdn.example.com/image1.png");
      when(msg2.getFileUrl()).thenReturn(null);

      doNothing().when(awsS3Service).deleteFile("image1.png");
      doNothing().when(messageRepository).deleteAllByChatId(chatId);

      messagesService.deleteAllByChat(chatId, userJwt);

      verify(chatService).findChatById(chatId);
      verify(awsS3Service).deleteFile("image1.png");
      verify(messageRepository).deleteAllByChatId(chatId);
    }

    @Test
    @DisplayName("deleteAllByChat - Should throw exception when user ID differs from chat user ID")
    void testDeleteAllByChat_ShouldThrowException_WhenUserIdIsDifferentFromChatUserId() {
      UUID chatId = UUID.randomUUID();
      var userId = UUID.randomUUID();
      var userJwt = createUserJwtData(userId.toString());
      var userChat = new User();
      userChat.setId(UUID.randomUUID()); // Different user ID
      
      Chat chat = mock(Chat.class);

      when(chatService.findChatById(chatId)).thenReturn(chat);
      when(chat.getUser()).thenReturn(userChat);

      assertThrows(ResponseStatusException.class, () -> messagesService.deleteAllByChat(chatId, userJwt));
      
      verify(chatService).findChatById(chatId);
      verify(messageRepository, never()).findAllByChatId(any());
      verify(messageRepository, never()).deleteAllByChatId(any());
    }
  }

  @Nested
  @DisplayName("IsChatNew Method")
  class IsChatNewTests {

    @Test
    @DisplayName("isChatNew - Should return true when messages are null or empty")
    void testIsChatNew_ReturnsTrueIfMessagesNullOrEmpty() {
      Chat chat = mock(Chat.class);
      when(chat.getMessages()).thenReturn(null);
      assertThat(invokeIsChatNew(chat)).isTrue();

      when(chat.getMessages()).thenReturn(Collections.emptyList());
      assertThat(invokeIsChatNew(chat)).isTrue();
    }

    @Test
    @DisplayName("isChatNew - Should return false when messages are not empty")
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
}
