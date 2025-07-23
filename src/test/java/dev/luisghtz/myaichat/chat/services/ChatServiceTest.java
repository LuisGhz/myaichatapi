package dev.luisghtz.myaichat.chat.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import dev.luisghtz.myaichat.auth.services.JwtService;
import dev.luisghtz.myaichat.auth.services.UserService;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.auth.entities.User;
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
  private UserService userService;

  @Mock
  private AIService aiProviderService;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private ChatService chatService;

  private Chat testChat;
  private UUID testChatId;
  private NewMessageRequestDto testRequest;
  private UserJwtDataDto testUser;

  @BeforeEach
  void setUp() {
    testChatId = UUID.randomUUID();

    // Create test user
    testUser = new UserJwtDataDto();
    testUser.setId(UUID.randomUUID().toString());
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");

    // Create test user entity
    User userEntity = new User();
    userEntity.setId(UUID.fromString(testUser.getId()));
    userEntity.setEmail(testUser.getEmail());
    userEntity.setUsername(testUser.getUsername());

    testChat = Chat.builder()
        .id(testChatId)
        .title("Test Chat")
        .createdAt(new Date())
        .model("gpt-4")
        .fav(false)
        .user(userEntity)
        .build();

    testRequest = new NewMessageRequestDto();
    testRequest.setModel("gpt-4");
    testRequest.setPrompt("Hello");
  }

  @Test
  void getAllChats_ShouldReturnChatsListResponseDto() {
    // Given
    List<Chat> chats = Arrays.asList(testChat);
    when(chatRepository.findAllByUserIdOrderByCreatedAtAsc(any(UUID.class))).thenReturn(chats);
    // When
    ChatsListResponseDto result = chatService.getAllChats(UUID.randomUUID().toString());

    // Then
    assertNotNull(result);
    assertEquals(1, result.getChats().size());
    assertEquals(testChatId, result.getChats().get(0).getId());
    assertEquals("Test Chat", result.getChats().get(0).getTitle());
    verify(chatRepository).findAllByUserIdOrderByCreatedAtAsc(any(UUID.class));
  }

  @Test
  void getChat_WithExistingChatId_ShouldReturnExistingChat() {
    // Given
    testRequest.setChatId(testChatId);
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
    // When
    Chat result = chatService.getChat(testRequest, testUser.getId());

    // Then
    assertEquals(testChat, result);
    verify(chatRepository).findById(testChatId);
  }

  @Test
  void getChat_WithNullChatId_ShouldCreateNewChat() {
    // Given
    testRequest.setChatId(null);
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);
    User userEntity = new User();
    userEntity.setId(UUID.fromString(testUser.getId()));
    userEntity.setEmail(testUser.getEmail());
    userEntity.setUsername(testUser.getUsername());
    when(userService.findById(any())).thenReturn(Optional.of(userEntity));
    // When
    Chat result = chatService.getChat(testRequest, testUser.getId());

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
    User userEntity = new User();
    userEntity.setId(UUID.fromString(testUser.getId()));
    userEntity.setEmail(testUser.getEmail());
    userEntity.setUsername(testUser.getUsername());
    when(userService.findById(any())).thenReturn(Optional.of(userEntity));
    // Given
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);
    // When
    Chat result = chatService.getNewChat(testRequest, testUser.getId());

    // Then
    assertNotNull(result);
    verify(chatRepository).save(any(Chat.class));
    verifyNoInteractions(customPromptService);
  }

  @Test
  void getNewChat_WithPromptId_ShouldCreateChatWithCustomPrompt() {
    User userEntity = new User();
    userEntity.setId(UUID.fromString(testUser.getId()));
    userEntity.setEmail(testUser.getEmail());
    userEntity.setUsername(testUser.getUsername());
    // Given
    var promptId = UUID.randomUUID();
    var promptIdString = promptId.toString();
    testRequest.setPromptId(promptIdString);
    
    CustomPrompt customPrompt = new CustomPrompt();
    customPrompt.setId(promptId);
    
    when(userService.findById(any())).thenReturn(Optional.of(userEntity));
    when(customPromptService.findById(promptIdString)).thenReturn(Optional.of(customPrompt));
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

    // When
    Chat result = chatService.getNewChat(testRequest, testUser.getId());

    // Then
    assertNotNull(result);
    verify(customPromptService).findById(promptIdString);
    verify(chatRepository).save(any(Chat.class));
  }

  @Test
  void getNewChat_WithInvalidPromptId_ShouldThrowException() {
    User userEntity = new User();
    userEntity.setId(UUID.fromString(testUser.getId()));
    userEntity.setEmail(testUser.getEmail());
    userEntity.setUsername(testUser.getUsername());
    // Given
    String promptId = "invalid-prompt";
    testRequest.setPromptId(promptId);
    
    when(userService.findById(any())).thenReturn(Optional.of(userEntity));
    when(customPromptService.findById(promptId)).thenReturn(Optional.empty());

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.getNewChat(testRequest, testUser.getId()));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getReason().contains("Prompt not found with ID: " + promptId));
    verify(customPromptService).findById(promptId);
    verifyNoInteractions(chatRepository);
  }

  @Test
  void getNewChat_WithEmptyPromptId_ShouldCreateBasicChat() {
    User userEntity = new User();
    userEntity.setId(UUID.fromString(testUser.getId()));
    userEntity.setEmail(testUser.getEmail());
    userEntity.setUsername(testUser.getUsername());
    // Given
    testRequest.setPromptId("");
    when(userService.findById(any())).thenReturn(Optional.of(userEntity));
    when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

    // When
    Chat result = chatService.getNewChat(testRequest, testUser.getId());

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
    chatService.deleteChat(testChatId, testUser);

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
        () -> chatService.deleteChat(testChatId, testUser));

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
    int result = chatService.renameChatTitleById(testChatId, newTitle, testUser);

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
        () -> chatService.renameChatTitleById(testChatId, newTitle, testUser));

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
    chatService.toggleChatFav(testChatId, testUser);
    boolean updatedFavStatus = testChat.getFav();
    verify(chatRepository).findById(testChatId);

    // Then
    assertNotEquals(initialFavStatus, updatedFavStatus);
    verify(chatRepository).setChatFav(testChatId, !initialFavStatus);
  }

  @Test
  void changeMaxOutputTokens_WithExistingId_ShouldChangeMaxOutputTokens() {
    // Given
    Short newMaxTokens = (short) 2000;
    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
    when(chatRepository.changeMaxTokens(testChatId, newMaxTokens)).thenReturn(1);

    // When
    int result = chatService.changeMaxOutputTokens(testChatId, newMaxTokens, testUser);

    // Then
    assertEquals(1, result);
    verify(chatRepository).findById(testChatId);
    verify(chatRepository).changeMaxTokens(testChatId, newMaxTokens);
  }

  @Test
  void changeMaxOutputTokens_WithNonExistentId_ShouldThrowException() {
    // Given
    Short newMaxTokens = (short) 2000;
    when(chatRepository.findById(testChatId)).thenReturn(Optional.empty());

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.changeMaxOutputTokens(testChatId, newMaxTokens, testUser));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getReason().contains("Chat not found with ID: " + testChatId));
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).changeMaxTokens(any(), any());
  }

  @Test
  void deleteChat_WithUnauthorizedUser_ShouldThrowForbiddenException() {
    // Given
    UserJwtDataDto unauthorizedUser = new UserJwtDataDto();
    unauthorizedUser.setId(UUID.randomUUID().toString());
    unauthorizedUser.setEmail("unauthorized@example.com");
    unauthorizedUser.setUsername("unauthorized");

    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.deleteChat(testChatId, unauthorizedUser));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("You don't have access to this chat", exception.getReason());
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).delete(any());
  }

  @Test
  void renameChatTitleById_WithUnauthorizedUser_ShouldThrowForbiddenException() {
    // Given
    String newTitle = "New Chat Title";
    UserJwtDataDto unauthorizedUser = new UserJwtDataDto();
    unauthorizedUser.setId(UUID.randomUUID().toString());
    unauthorizedUser.setEmail("unauthorized@example.com");
    unauthorizedUser.setUsername("unauthorized");

    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.renameChatTitleById(testChatId, newTitle, unauthorizedUser));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("You don't have access to this chat", exception.getReason());
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).renameChatTitleById(any(), any());
  }

  @Test
  void changeMaxOutputTokens_WithUnauthorizedUser_ShouldThrowForbiddenException() {
    // Given
    Short newMaxTokens = (short) 2000;
    UserJwtDataDto unauthorizedUser = new UserJwtDataDto();
    unauthorizedUser.setId(UUID.randomUUID().toString());
    unauthorizedUser.setEmail("unauthorized@example.com");
    unauthorizedUser.setUsername("unauthorized");

    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.changeMaxOutputTokens(testChatId, newMaxTokens, unauthorizedUser));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("You don't have access to this chat", exception.getReason());
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).changeMaxTokens(any(), any());
  }

  @Test
  void toggleChatFav_WithUnauthorizedUser_ShouldThrowForbiddenException() {
    // Given
    UserJwtDataDto unauthorizedUser = new UserJwtDataDto();
    unauthorizedUser.setId(UUID.randomUUID().toString());
    unauthorizedUser.setEmail("unauthorized@example.com");
    unauthorizedUser.setUsername("unauthorized");

    when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

    // When & Then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> chatService.toggleChatFav(testChatId, unauthorizedUser));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("You don't have access to this chat", exception.getReason());
    verify(chatRepository).findById(testChatId);
    verify(chatRepository, never()).setChatFav(any(), any());
  }
}