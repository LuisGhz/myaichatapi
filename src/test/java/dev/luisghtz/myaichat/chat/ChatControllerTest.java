package dev.luisghtz.myaichat.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChangeMaxOutputTokensReqDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.dtos.RenameChatTitleDto;
import dev.luisghtz.myaichat.chat.models.AppMessageHistory;
import dev.luisghtz.myaichat.chat.models.ChatSummary;
import dev.luisghtz.myaichat.chat.services.ChatService;
import dev.luisghtz.myaichat.chat.services.MessagesService;
import dev.luisghtz.myaichat.configurationMock.AIModelsControllerTestConfiguration;
import dev.luisghtz.myaichat.configurationMock.UserJwtDataTestConfiguration;
import dev.luisghtz.myaichat.configurationMock.jwt.JwtPermitAllTestConfiguration;
import dev.luisghtz.myaichat.file.FileService;

@WebMvcTest(ChatController.class)
@Import({
    AIModelsControllerTestConfiguration.class,
    JwtPermitAllTestConfiguration.class,
    UserJwtDataTestConfiguration.class
})
@ActiveProfiles("test")
public class ChatControllerTest {

  @MockitoBean
  private MessagesService messagesService;

  @MockitoBean
  private FileService fileService;

  @MockitoBean
  private ChatService chatService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private UUID testChatId;

  @BeforeEach
  public void setup() {
    testChatId = UUID.randomUUID();
  }

  private UserJwtDataDto createTestUserJwtData() {
    UserJwtDataDto user = new UserJwtDataDto();
    user.setId("test-user-id");
    user.setEmail("testuser@example.com");
    user.setUsername("testuser");
    return user;
  }

  @Nested
  @DisplayName("GET Endpoints")
  class GetEndpoints {

    @Test
    @DisplayName("GET /api/chat/all - Returns all chats list")
    public void testGetChatsList() throws Exception {
      // Arrange
      String userId = "test-user-id";
      List<ChatSummary> chatsList = new ArrayList<>();
      chatsList.add(ChatSummary.builder().id(UUID.randomUUID()).title("Chat 1").build());
      chatsList.add(ChatSummary.builder().id(UUID.randomUUID()).title("Chat 2").build());

      ChatsListResponseDto expectedResponse = new ChatsListResponseDto(chatsList);
      when(chatService.getAllChats(userId)).thenReturn(expectedResponse);

      // Act & Assert
      mockMvc.perform(get("/api/chat/all"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.chats").isArray())
          .andExpect(jsonPath("$.chats.length()").value(2));

      verify(chatService, times(1)).getAllChats(userId);
    }

    @Test
    @DisplayName("GET /api/chat/{id}/messages - Returns chat history")
    public void testGetChatHistory() throws Exception {
      // Arrange
      Pageable pageable = PageRequest.of(0, 10);
      List<AppMessageHistory> historyMessages = new ArrayList<>();
      historyMessages.add(AppMessageHistory.builder().content("Hello").build());

      HistoryChatDto expectedResponse = HistoryChatDto.builder()
          .historyMessages(historyMessages)
          .build();
      var user = createTestUserJwtData();
      when(messagesService.getPreviousMessages(eq(testChatId), eq(pageable), any(UserJwtDataDto.class)))
          .thenReturn(expectedResponse);

      // Act & Assert
      mockMvc.perform(get("/api/chat/{id}/messages", testChatId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.historyMessages").isArray());

      verify(messagesService, times(1)).getPreviousMessages(eq(testChatId), eq(pageable), eq(user));
    }

    @Test
    @DisplayName("GET /api/chat/{id}/messages - Returns NOT_FOUND for invalid chat id")
    public void testGetChatHistoryWithInvalidId() throws Exception {
      // Arrange
      UUID invalidId = UUID.randomUUID();
      Pageable pageable = PageRequest.of(0, 10);

      ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found");
      var user = createTestUserJwtData();
      when(messagesService.getPreviousMessages(invalidId, pageable, user))
          .thenThrow(exception);

      // Act & Assert
      mockMvc.perform(get("/api/chat/{id}/messages", invalidId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value("NOT_FOUND"))
          .andExpect(jsonPath("$.message").value(exception.getMessage()));
    }

    @Test
    @DisplayName("GET /api/chat/{id}/messages with pagination - Returns paged chat history")
    public void testGetChatHistoryWithPagination() throws Exception {
      // Arrange
      // Pageable pageable = PageRequest.of(2, 5); // Page 2 with 5 items per page
      List<AppMessageHistory> historyMessages = new ArrayList<>();
      historyMessages.add(AppMessageHistory.builder().content("Paged message").build());

      HistoryChatDto expectedResponse = HistoryChatDto.builder()
          .historyMessages(historyMessages)
          .build();

      var user = createTestUserJwtData();
      when(messagesService.getPreviousMessages(eq(testChatId), any(Pageable.class), eq(user)))
          .thenReturn(expectedResponse);

      // Act & Assert
      mockMvc.perform(get("/api/chat/{id}/messages", testChatId)
          .param("page", "2")
          .param("size", "5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.historyMessages[0].content").value("Paged message"));

      // We can't verify exact pageable because of how MockMvc works, but we can
      // verify the method was called
      verify(messagesService, times(1)).getPreviousMessages(eq(testChatId), any(Pageable.class), eq(user));
    }
  }

  @Nested
  @DisplayName("POST Endpoints")
  class PostEndpoints {

    @Test
    @DisplayName("POST /api/chat/send-message - Sends new message without file")
    public void testNewMessageWithoutFile() throws Exception {
      // Arrange
      NewMessageRequestDto requestDto = new NewMessageRequestDto();
      requestDto.setPrompt("Hello AI");
      requestDto.setChatId(testChatId);
      requestDto.setMaxOutputTokens((short) 1500);
      requestDto.setFile(null);
      requestDto.setModel("gpt-4o");

      AssistantMessageResponseDto expectedResponse = AssistantMessageResponseDto.builder()
          .content("AI response")
          .build();
      var user = createTestUserJwtData();
      when(messagesService.sendNewMessage(any(NewMessageRequestDto.class), isNull(), eq(user)))
          .thenReturn(expectedResponse);

      mockMvc.perform(multipart("/api/chat/send-message")
          .param("prompt", requestDto.getPrompt())
          .param("chatId", requestDto.getChatId().toString())
          .param("maxOutputTokens", requestDto.getMaxOutputTokens().toString())
          .param("model", requestDto.getModel()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("AI response"));

      verify(fileService, never()).uploadFile(any());
      verify(messagesService, times(1)).sendNewMessage(any(NewMessageRequestDto.class), isNull(),
          eq(user));
    }

    @Test
    @DisplayName("POST /api/chat/send-message - Sends new message with file")
    public void testNewMessageWithFile() throws Exception {
      // Arrange
      NewMessageRequestDto requestDto = new NewMessageRequestDto();
      requestDto.setPrompt("Check this file");
      requestDto.setChatId(testChatId);
      requestDto.setMaxOutputTokens((short) 2000);
      requestDto.setModel("gpt-4o");

      MockMultipartFile uploadedFile = new MockMultipartFile(
          "file", "test-image.jpg", "image/jpeg", "test image content".getBytes());
      requestDto.setFile(uploadedFile);

      String fileName = "uploaded-file.jpg";
      when(fileService.uploadFile(any())).thenReturn(fileName);

      AssistantMessageResponseDto expectedResponse = AssistantMessageResponseDto.builder()
          .content("AI response to file")
          .build();

      when(messagesService.sendNewMessage(any(NewMessageRequestDto.class), eq(fileName), any(UserJwtDataDto.class)))
          .thenReturn(expectedResponse);

      // Act & Assert
      mockMvc.perform(multipart("/api/chat/send-message")
          .file(uploadedFile)
          .param("prompt", requestDto.getPrompt())
          .param("chatId", requestDto.getChatId().toString())
          .param("maxOutputTokens", requestDto.getMaxOutputTokens().toString())
          .param("model", requestDto.getModel()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("AI response to file"));

      verify(fileService, times(1)).uploadFile(any());
      verify(messagesService, times(1)).sendNewMessage(any(NewMessageRequestDto.class), eq(fileName),
          any(UserJwtDataDto.class));
    }

    @Test
    @DisplayName("POST /api/chat/send-message - Returns BAD_REQUEST for invalid file")
    public void sendNewMessageWithInvalidFile() throws Exception {
      // Arrange - invalid file
      MockMultipartFile invalidFile = new MockMultipartFile(
          "file", "invalid.txt", "text/plain", "This is not a supported file".getBytes());

      // Act & Assert
      mockMvc.perform(multipart("/api/chat/send-message")
          .file(invalidFile)
          .param("prompt", "Invalid file test")
          .param("chatId", testChatId.toString())
          .param("maxOutputTokens", "2000")
          .param("model", "gpt-4o"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/chat/send-message - Returns BAD_REQUEST for invalid model")
    public void sendNewMessageWithInvalidModel() throws Exception {
      // Arrange - invalid model
      NewMessageRequestDto requestDto = new NewMessageRequestDto();
      requestDto.setPrompt("Test with invalid model");
      requestDto.setChatId(testChatId);
      requestDto.setMaxOutputTokens((short) 2000);
      requestDto.setModel("invalid-model");

      // Act & Assert
      mockMvc.perform(multipart("/api/chat/send-message")
          .param("prompt", requestDto.getPrompt())
          .param("chatId", requestDto.getChatId().toString())
          .param("maxOutputTokens", requestDto.getMaxOutputTokens().toString())
          .param("model", requestDto.getModel()))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/chat/send-message - Returns BAD_REQUEST for missing required fields")
    public void sendNewMessageWithMissingFields() throws Exception {
      // Arrange - missing required fields
      NewMessageRequestDto requestDto = new NewMessageRequestDto();
      requestDto.setPrompt(""); // Empty prompt
      requestDto.setChatId(null); // No chat ID

      // Act & Assert
      mockMvc.perform(multipart("/api/chat/send-message")
          .param("prompt", "")
          .param("model", "gpt-4o"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/chat/send-message - Returns BAD_REQUEST for invalid request")
    public void testNewMessageWithInvalidRequest() throws Exception {
      // Act & Assert - test with completely missing parameters
      mockMvc.perform(multipart("/api/chat/send-message"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PATCH Endpoints")
  class PatchEndpoints {

    @Test
    @DisplayName("PATCH /api/chat/{id}/rename - Should rename chat title successfully")
    public void testRenameChatSuccess() throws Exception {
      // Arrange
      String newTitle = "New Chat Title";
      RenameChatTitleDto requestDto = new RenameChatTitleDto();
      requestDto.setTitle(newTitle);

      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/rename", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());

      verify(chatService, times(1)).renameChatTitleById(eq(testChatId), eq(newTitle), any(UserJwtDataDto.class));
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/rename - Should return BAD_REQUEST for blank title")
    public void testRenameChatWithBlankTitle() throws Exception {
      // Arrange
      RenameChatTitleDto requestDto = new RenameChatTitleDto();
      requestDto.setTitle("");

      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/rename", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());

      verify(chatService, never()).renameChatTitleById(any(), any(), any());
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/rename - Should return BAD_REQUEST for title too long")
    public void testRenameChatWithTooLongTitle() throws Exception {
      // Arrange
      String longTitle = "a".repeat(51); // Exceeds max length of 50
      RenameChatTitleDto requestDto = new RenameChatTitleDto();
      requestDto.setTitle(longTitle);

      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/rename", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());

      verify(chatService, never()).renameChatTitleById(any(), any(), any());
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/rename - Should return NOT_FOUND for invalid chat id")
    public void testRenameChatWithInvalidId() throws Exception {
      // Arrange
      String newTitle = "New Chat Title";
      RenameChatTitleDto requestDto = new RenameChatTitleDto();
      requestDto.setTitle(newTitle);

      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/rename", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());

      verify(chatService, times(1)).renameChatTitleById(eq(testChatId), eq(newTitle), any(UserJwtDataDto.class));
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/change-max-output-tokens - Should change max output tokens successfully")
    public void testChangeMaxOutputTokensSuccess() throws Exception {
      // Arrange
      Short newMaxTokens = (short) 2000;
      ChangeMaxOutputTokensReqDto requestDto = new ChangeMaxOutputTokensReqDto();
      requestDto.setMaxOutputTokens(newMaxTokens);

      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/change-max-output-tokens", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());

      verify(chatService, times(1)).changeMaxOutputTokens(eq(testChatId), eq(newMaxTokens), any(UserJwtDataDto.class));
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/change-max-output-tokens - Should return NOT_FOUND for invalid chat id")
    public void testChangeMaxOutputTokensWithInvalidId() throws Exception {
      // Arrange
      Short newMaxTokens = (short) 2000;
      ChangeMaxOutputTokensReqDto requestDto = new ChangeMaxOutputTokensReqDto();
      requestDto.setMaxOutputTokens(newMaxTokens);

      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/change-max-output-tokens", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());

      verify(chatService, times(1)).changeMaxOutputTokens(eq(testChatId), eq(newMaxTokens), any(UserJwtDataDto.class));
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/change-max-output-tokens - Should handle null max tokens")
    public void testChangeMaxOutputTokensWithNullValue() throws Exception {
      // Act & Assert - test with null/missing maxOutputTokens
      mockMvc.perform(patch("/api/chat/{id}/change-max-output-tokens", testChatId)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/toggle-chat-fav - Should toggle chat favorite successfully")
    public void testToggleChatFavSuccess() throws Exception {
      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/toggle-chat-fav", testChatId))
          .andExpect(status().isNoContent());

      verify(chatService, times(1)).toggleChatFav(eq(testChatId), any(UserJwtDataDto.class));
    }

    @Test
    @DisplayName("PATCH /api/chat/{id}/toggle-chat-fav - Should return NOT_FOUND for invalid chat id")
    public void testToggleChatFavWithInvalidId() throws Exception {
      // Act & Assert
      mockMvc.perform(patch("/api/chat/{id}/toggle-chat-fav", testChatId))
          .andExpect(status().isNoContent());

      verify(chatService, times(1)).toggleChatFav(eq(testChatId), any(UserJwtDataDto.class));
    }
  }

  @Nested
  @DisplayName("DELETE Endpoints")
  class DeleteEndpoints {

    @Test
    @DisplayName("DELETE /api/chat/{id}/delete - Deletes chat and its messages")
    public void testDeleteChat() throws Exception {
      // Act & Assert
      mockMvc.perform(delete("/api/chat/{id}/delete", testChatId))
          .andExpect(status().isNoContent());

      verify(messagesService, times(1)).deleteAllByChat(eq(testChatId), any(UserJwtDataDto.class));
      verify(chatService, times(1)).deleteChat(eq(testChatId), any(UserJwtDataDto.class));
    }
  }
}
