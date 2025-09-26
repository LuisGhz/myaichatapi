package dev.luisghtz.myaichat.chat.services;

import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.auth.entities.User;

import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.models.TokensSum;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;
import dev.luisghtz.myaichat.chat.repositories.MessageRepository;

import dev.luisghtz.myaichat.file.providers.AwsS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

  @Nested
  @DisplayName("GetAssistantMessage Method")
  class GetAssistantMessageTests {

    @Test
    @DisplayName("getAssistantMessage - Should stream chunks and on last chunk save assistant message and generate title for new chat")
    void testGetAssistantMessage_StreamsAndSavesAndGeneratesTitle() throws Exception {
      UUID chatId = UUID.randomUUID();
      var userId = UUID.randomUUID();
      var userJwt = createUserJwtData(userId.toString());

      // Prepare chat and messages
      Chat chat = mock(Chat.class);
      when(chatService.findChatById(chatId)).thenReturn(chat);
      when(chat.getUser()).thenReturn(new User() {{ setId(userId); }});
      when(chat.getModel()).thenReturn("gpt-3");
      when(chat.getTitle()).thenReturn(null); // new chat
      when(chat.getId()).thenReturn(chatId);

      AppMessage userMsg = mock(AppMessage.class);
      when(userMsg.getRole()).thenReturn("User");
      when(userMsg.getContent()).thenReturn("Hello");
      when(chat.getMessages()).thenReturn(List.of(userMsg));

      // Build two ChatResponse-like mocks: first chunk (not last), second chunk (last with usage)
  var chunk1 = mock(org.springframework.ai.chat.model.ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
  when(chunk1.getResult().getOutput().getText()).thenReturn("Hello ");
  when(chunk1.getMetadata().getUsage().getTotalTokens()).thenReturn(0);

  var chunk2 = mock(org.springframework.ai.chat.model.ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
  when(chunk2.getResult().getOutput().getText()).thenReturn("world");
  when(chunk2.getMetadata().getUsage().getTotalTokens()).thenReturn(3);
  when(chunk2.getMetadata().getUsage().getPromptTokens()).thenReturn(1);
  when(chunk2.getMetadata().getUsage().getCompletionTokens()).thenReturn(2);
  // no-op: deep stub provides nested usage mock

      when(aiProviderService.getAssistantMessage(anyList(), any())).thenReturn(
          reactor.core.publisher.Flux.just(chunk1, chunk2)
      );

  lenient().when(aiProviderService.generateTitle(eq(chat), anyString(), anyString())).thenReturn("Generated Title");

  // Stub token sums repository call used after save
  lenient().when(messageRepository.getSumOfPromptAndCompletionTokensByChatId(chatId)).thenReturn(new TokensSum(1L, 2L));

  // Capture repository saves
  lenient().when(messageRepository.save(any(AppMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

      var responses = messagesService.getAssistantMessage(chatId, userJwt).collectList().block();

      assertThat(responses).isNotNull();
      assertThat(responses).hasSize(2);
  assertThat(responses.get(0).getIsLastChunk()).isFalse();
  assertThat(responses.get(1).getIsLastChunk()).isTrue();
      assertThat(responses.get(1).getContent()).isEqualTo("world");
      assertThat(responses.get(1).getChatTitle()).isEqualTo("Generated Title");

      // Allow asynchronous DB saves and title update to run
      Thread.sleep(200);

      verify(messageRepository, atLeastOnce()).save(any(AppMessage.class));
      verify(chatService, atLeastOnce()).updateChatTitle(eq(chatId), eq("Generated Title"));
    }

    @Test
    @DisplayName("getAssistantMessage - Should log and return last chunk even when no user message exists")
    void testGetAssistantMessage_NoUserMessageHandledGracefully() {
      UUID chatId = UUID.randomUUID();
      var userId = UUID.randomUUID();
      var userJwt = createUserJwtData(userId.toString());

      Chat chat = mock(Chat.class);
      when(chatService.findChatById(chatId)).thenReturn(chat);
      when(chat.getUser()).thenReturn(new User() {{ setId(userId); }});
      when(chat.getModel()).thenReturn("gpt-3");
      when(chat.getTitle()).thenReturn("Existing Title");
      when(chat.getId()).thenReturn(chatId);
      when(chat.getMessages()).thenReturn(Collections.emptyList());

  var chunk = mock(org.springframework.ai.chat.model.ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
  when(chunk.getResult().getOutput().getText()).thenReturn("Only");
  when(chunk.getMetadata().getUsage().getTotalTokens()).thenReturn(1);
  when(chunk.getMetadata().getUsage().getPromptTokens()).thenReturn(0);
  when(chunk.getMetadata().getUsage().getCompletionTokens()).thenReturn(1);

  when(aiProviderService.getAssistantMessage(anyList(), any())).thenReturn(reactor.core.publisher.Flux.just(chunk));
  lenient().when(messageRepository.getSumOfPromptAndCompletionTokensByChatId(chatId)).thenReturn(new TokensSum(0L, 1L));
  lenient().when(messageRepository.save(any(AppMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

      var responses = messagesService.getAssistantMessage(chatId, userJwt).collectList().block();

      assertThat(responses).isNotNull();
      assertThat(responses).hasSize(1);
  assertThat(responses.get(0).getIsLastChunk()).isTrue();
      assertThat(responses.get(0).getChatTitle()).isEqualTo("Existing Title");
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
