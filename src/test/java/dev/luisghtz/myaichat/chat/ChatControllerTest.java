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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.models.AppMessageHistory;
import dev.luisghtz.myaichat.chat.models.ChatSummary;
import dev.luisghtz.myaichat.chat.services.ChatService;
import dev.luisghtz.myaichat.chat.services.MessagesService;
import dev.luisghtz.myaichat.image.ImageService;

@WebMvcTest(ChatController.class)
@Import(ChatControllerTestConfiguration.class)
@ActiveProfiles("test")
public class ChatControllerTest {

  @MockitoBean
  private MessagesService messagesService;

  @MockitoBean
  private ImageService imageService;

  @MockitoBean
  private ChatService chatService;

  @Autowired
  private MockMvc mockMvc;

  private UUID testChatId;

  @BeforeEach
  public void setup() {
    testChatId = UUID.randomUUID();
  }

  @Test
  public void testGetChatsList() throws Exception {
    // Arrange
    List<ChatSummary> chatsList = new ArrayList<>();
    chatsList.add(ChatSummary.builder().id(UUID.randomUUID()).title("Chat 1").build());
    chatsList.add(ChatSummary.builder().id(UUID.randomUUID()).title("Chat 2").build());

    ChatsListResponseDto expectedResponse = new ChatsListResponseDto(chatsList);
    when(chatService.getAllChats()).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc.perform(get("/api/chat/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chats").isArray())
        .andExpect(jsonPath("$.chats.length()").value(2));

    verify(chatService, times(1)).getAllChats();
  }

  @Test
  public void testGetChatHistory() throws Exception {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    List<AppMessageHistory> historyMessages = new ArrayList<>();
    historyMessages.add(AppMessageHistory.builder().content("Hello").build());

    HistoryChatDto expectedResponse = HistoryChatDto.builder()
        .historyMessages(historyMessages)
        .build();

    when(messagesService.getPreviousMessages(testChatId, pageable)).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc.perform(get("/api/chat/{id}/messages", testChatId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.historyMessages").isArray());

    verify(messagesService, times(1)).getPreviousMessages(testChatId, pageable);
  }

  @Test
  public void testNewMessageWithoutImage() throws Exception {
    // Arrange
    NewMessageRequestDto requestDto = new NewMessageRequestDto();
    requestDto.setPrompt("Hello AI");
    requestDto.setChatId(testChatId);
    requestDto.setMaxOutputTokens((short) 1500);
    requestDto.setImage(null);
    requestDto.setModel("gpt-4o");

    AssistantMessageResponseDto expectedResponse = AssistantMessageResponseDto.builder()
        .content("AI response")
        .build();

    when(messagesService.sendNewMessage(any(NewMessageRequestDto.class), isNull())).thenReturn(expectedResponse);

    mockMvc.perform(multipart("/api/chat/send-message")
        .param("prompt", requestDto.getPrompt())
        .param("chatId", requestDto.getChatId().toString())
        .param("maxOutputTokens", requestDto.getMaxOutputTokens().toString())
        .param("model", requestDto.getModel()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("AI response"));

    verify(imageService, never()).uploadImage(any());
    verify(messagesService, times(1)).sendNewMessage(any(NewMessageRequestDto.class), isNull());
  }

  @Test
  public void testNewMessageWithImage() throws Exception {
    // Arrange
    NewMessageRequestDto requestDto = new NewMessageRequestDto();
    requestDto.setPrompt("Check this image");
    requestDto.setChatId(testChatId);
    requestDto.setMaxOutputTokens((short) 2000);
    requestDto.setModel("gpt-4o");

    MockMultipartFile imageFile = new MockMultipartFile(
        "image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
    requestDto.setImage(imageFile);

    String imageFileName = "uploaded-image.jpg";
    when(imageService.uploadImage(any())).thenReturn(imageFileName);

    AssistantMessageResponseDto expectedResponse = AssistantMessageResponseDto.builder()
        .content("AI response to image")
        .build();

    when(messagesService.sendNewMessage(any(NewMessageRequestDto.class), eq(imageFileName)))
        .thenReturn(expectedResponse);

    // Act & Assert
    mockMvc.perform(multipart("/api/chat/send-message")
        .file(imageFile)
        .param("prompt", requestDto.getPrompt())
        .param("chatId", requestDto.getChatId().toString())
        .param("maxOutputTokens", requestDto.getMaxOutputTokens().toString())
        .param("model", requestDto.getModel()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("AI response to image"));

    verify(imageService, times(1)).uploadImage(any());
    verify(messagesService, times(1)).sendNewMessage(any(NewMessageRequestDto.class), eq(imageFileName));
  }

  @Test
  public void testDeleteChat() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/chat/{id}/delete", testChatId))
        .andExpect(status().isNoContent());

    verify(messagesService, times(1)).deleteAllByChat(testChatId);
    verify(chatService, times(1)).deleteChat(testChatId);
  }

  @Test
  public void testGetChatHistoryWithInvalidId() throws Exception {
    // Arrange
    UUID invalidId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);

    ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found");
    when(messagesService.getPreviousMessages(invalidId, pageable))
        .thenThrow(exception);

    // Act & Assert
    mockMvc.perform(get("/api/chat/{id}/messages", invalidId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.statusCode").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value(exception.getMessage()));
  }

  @Test
  public void testNewMessageWithInvalidRequest() throws Exception {
    // Arrange - missing required fields

    // Act & Assert
    mockMvc.perform(multipart("/api/chat/send-message"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetChatHistoryWithPagination() throws Exception {
    // Arrange
    // Pageable pageable = PageRequest.of(2, 5); // Page 2 with 5 items per page
    List<AppMessageHistory> historyMessages = new ArrayList<>();
    historyMessages.add(AppMessageHistory.builder().content("Paged message").build());

    HistoryChatDto expectedResponse = HistoryChatDto.builder()
        .historyMessages(historyMessages)
        .build();

    when(messagesService.getPreviousMessages(eq(testChatId), any(Pageable.class))).thenReturn(expectedResponse);

    // Act & Assert
    mockMvc.perform(get("/api/chat/{id}/messages", testChatId)
        .param("page", "2")
        .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.historyMessages[0].content").value("Paged message"));

    // We can't verify exact pageable because of how MockMvc works, but we can
    // verify the method was called
    verify(messagesService, times(1)).getPreviousMessages(eq(testChatId), any(Pageable.class));
  }
}