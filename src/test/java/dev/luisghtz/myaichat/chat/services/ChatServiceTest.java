package dev.luisghtz.myaichat.chat.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import dev.luisghtz.myaichat.ai.services.AIService;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.repositories.ChatRepository;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock
  private ChatRepository chatRepository;

  @Mock
  private CustomPromptService customPromptService;

  @Mock
  private AIService aiProviderService;

  @InjectMocks
  private ChatService chatService;

  private Chat testChat;
  private UUID testChatId;
  private NewMessageRequestDto testRequest;

  @BeforeEach
  void setUp() {
    testChatId = UUID.randomUUID();
    testChat = Chat.builder()
        .id(testChatId)
        .title("Test Chat")
        .createdAt(new Date())
        .model("gpt-4")
        .fav(false)
        .build();

    testRequest = new NewMessageRequestDto();
    testRequest.setModel("gpt-4");
    testRequest.setPrompt("Hello");
  }

  @Test
  void getAllChats_ShouldReturnChatsListResponseDto() {
    // Given
    List<Chat> chats = Arrays.asList(testChat);
    when(chatRepository.findAllByOrderByCreatedAtAsc()).thenReturn(chats);

    // When
    ChatsListResponseDto result = chatService.getAllChats();

    // Then
    assertNotNull(result);
    assertEquals(1, result.getChats().size());
    assertEquals(testChatId, result.getChats().get(0).getId());
    assertEquals("Test Chat", result.getChats().get(0).getTitle());
    verify(chatRepository).findAllByOrderByCreatedAtAsc();
  }

  @Test
  void getChat_WithExistingChatId_ShouldReturnExistingChat() {
    // Given
    testRequest.setChatId(testChatId);
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When
    Chat result = chatService.getChat(testRequest);

    // Then
    assertEquals(testChat, result);
    verify(chatRepository).findById(testChatId);
  }

  @Test
  void getChat_WithNullChatId_ShouldCreateNewChat() {
    // Given
    testRequest.setChatId(null);
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

    // When
    Chat result = chatService.getChat(testRequest);

    // Then
    assertNotNull(result);
    verify(chatRepository).save(any(Chat.class));
  }

  @Test
  void findChatById_WithExistingId_ShouldReturnChat() {
    // Given
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When
    Chat result = chatService.findChatById(testChatId);

    // Then
    assertEquals(testChat, result);
    verify(chatRepository).findById(testChatId);
  }

  @Test
  void findChatById_WithNonExistentId_ShouldThrowException() {
    // Given
    when(chatRepository.findById(testChatId)).thenReturn(Optional.empty());

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.findChatById(testChatId));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getReason().contains("Chat not found with ID: " + testChatId));
    verify(chatRepository).findById(testChatId);
  }

  @Test
  void generateAndSetTitleForNewChat_ShouldUpdateChatTitle() {
    // Given
    String generatedTitle = "Generated Title";
    AssistantMessageResponseDto response = new AssistantMessageResponseDto();
    response.setContent("Response content");

    when(aiProviderService.generateTitle(testChat, testRequest.getPrompt(), response.getContent()))
        .thenReturn(generatedTitle);
    when(chatRepository.save(testChat)).thenReturn(testChat);

    // When
    chatService.generateAndSetTitleForNewChat(testChat, testRequest, response);

    // Then
    assertEquals(generatedTitle, testChat.getTitle());
    assertEquals(generatedTitle, response.getChatTitle());
    verify(aiProviderService).generateTitle(testChat, testRequest.getPrompt(), response.getContent());
    verify(chatRepository).save(testChat);
  }

  @Test
  void getNewChat_WithoutPromptId_ShouldCreateBasicChat() {
    // Given
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

    // When
    Chat result = chatService.getNewChat(testRequest);

    // Then
    assertNotNull(result);
    verify(chatRepository).save(any(Chat.class));
    verifyNoInteractions(customPromptService);
  }

  @Test
  void getNewChat_WithPromptId_ShouldCreateChatWithCustomPrompt() {
    // Given
    var promptId = UUID.randomUUID();
    var promptIdString = promptId.toString();
    testRequest.setPromptId(promptIdString);

    CustomPrompt customPrompt = new CustomPrompt();
    customPrompt.setId(promptId);

    when(customPromptService.findById(promptIdString)).thenReturn(Optional.of(customPrompt));
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

    // When
    Chat result = chatService.getNewChat(testRequest);

    // Then
    assertNotNull(result);
    verify(customPromptService).findById(promptIdString);
    verify(chatRepository).save(any(Chat.class));
  }

  @Test
  void getNewChat_WithInvalidPromptId_ShouldThrowException() {
    // Given
    String promptId = "invalid-prompt";
    testRequest.setPromptId(promptId);

    when(customPromptService.findById(promptId)).thenReturn(Optional.empty());

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.getNewChat(testRequest));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getReason().contains("Prompt not found with ID: " + promptId));
    verify(customPromptService).findById(promptId);
    verifyNoInteractions(chatRepository);
  }

  @Test
  void getNewChat_WithEmptyPromptId_ShouldCreateBasicChat() {
    // Given
    testRequest.setPromptId("");
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

    // When
    Chat result = chatService.getNewChat(testRequest);

    // Then
    assertNotNull(result);
    verify(chatRepository).save(any(Chat.class));
    verifyNoInteractions(customPromptService);
  }

  @Test
  void deleteChat_WithExistingId_ShouldDeleteChat() {
    // Given
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When
    chatService.deleteChat(testChatId);

    // Then
    verify(chatRepository).findById(testChatId);
    verify(chatRepository).delete(testChat);
  }

  @Test
  void deleteChat_WithNonExistentId_ShouldThrowException() {
    // Given
    when(chatRepository.findById(testChatId)).thenReturn(Optional.empty());

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.deleteChat(testChatId));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).delete(any());
  }

  @Test
  void renameChatTitleById_WithExistingId_ShouldRenameChatTitle() {
    // Given
    String newTitle = "New Chat Title";
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
    when(chatRepository.renameChatTitleById(testChatId, newTitle)).thenReturn(1);

    // When
    int result = chatService.renameChatTitleById(testChatId, newTitle);

    // Then
    assertEquals(1, result);
    verify(chatRepository).findById(testChatId);
    verify(chatRepository).renameChatTitleById(testChatId, newTitle);
  }

  @Test
  void renameChatTitleById_WithNonExistentId_ShouldThrowException() {
    // Given
    String newTitle = "New Chat Title";
    when(chatRepository.findById(testChatId)).thenReturn(Optional.empty());

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.renameChatTitleById(testChatId, newTitle));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getReason().contains("Chat not found with ID: " + testChatId));
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).renameChatTitleById(any(), any());
  }

  @Test
  void toggleChatFav_ShouldToggleFavoriteStatus() {
    // Given
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
    boolean initialFavStatus = testChat.getFav();

    // When
    chatService.toggleChatFav(testChatId);
    boolean updatedFavStatus = testChat.getFav();
    verify(chatRepository).findById(testChatId);

    // Then
    assertNotEquals(initialFavStatus, updatedFavStatus);
    verify(chatRepository).setChatFav(testChatId, !initialFavStatus);
  }
}