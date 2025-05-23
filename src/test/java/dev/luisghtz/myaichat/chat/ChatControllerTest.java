package dev.luisghtz.myaichat.chat;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.models.ChatSummary;
import dev.luisghtz.myaichat.chat.services.ChatService;
import dev.luisghtz.myaichat.chat.services.MessagesService;
import dev.luisghtz.myaichat.image.ImageService;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

  @Mock
  private MessagesService aiService;

  @Mock
  private ImageService imageService;

  @Mock
  private ChatService chatService;

  @Mock
  private MessagesService messagesService;

  // @InjectMocks
  // private ChatController chatController;

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

  // @Test
  // public void testGetChatHistory() throws Exception {
  //   // Arrange
  //   Pageable pageable = PageRequest.of(0, 10);
  //   List<MessageDto> messages = new ArrayList<>();
  //   messages.add(MessageDto.builder().id(UUID.randomUUID()).content("Hello").build());

  //   HistoryChatDto expectedResponse = HistoryChatDto.builder()
  //       .messages(messages)
  //       .chatId(testChatId)
  //       .build();

  //   when(aiService.getPreviousMessages(testChatId, pageable)).thenReturn(expectedResponse);

  //   // Act & Assert
  //   mockMvc.perform(get("/api/chat/{id}/messages", testChatId))
  //       .andExpect(status().isOk())
  //       .andExpect(jsonPath("$.chatId").value(testChatId.toString()))
  //       .andExpect(jsonPath("$.messages").isArray());

  //   verify(aiService, times(1)).getPreviousMessages(testChatId, pageable);
  // }

  // @Test
  // public void testNewMessageWithoutImage() throws Exception {
  //   // Arrange
  //   NewMessageRequestDto requestDto = new NewMessageRequestDto();
  //   requestDto.setMessage("Hello AI");
  //   requestDto.setChatId(testChatId);

  //   AssistantMessageResponseDto expectedResponse = AssistantMessageResponseDto.builder()
  //       .message("AI response")
  //       .build();

  //   when(aiService.sendNewMessage(any(NewMessageRequestDto.class), isNull())).thenReturn(expectedResponse);

  //   // Act & Assert
  //   MockMultipartFile messageFile = new MockMultipartFile(
  //       "message", "", "text/plain", "Hello AI".getBytes());

  //   MockMultipartFile chatIdFile = new MockMultipartFile(
  //       "chatId", "", "text/plain", testChatId.toString().getBytes());

  //   mockMvc.perform(multipart("/api/chat/send-message")
  //       .file(messageFile)
  //       .file(chatIdFile))
  //       .andExpect(status().isOk())
  //       .andExpect(jsonPath("$.message").value("AI response"));

  //   verify(imageService, never()).uploadImage(any());
  //   verify(aiService, times(1)).sendNewMessage(any(NewMessageRequestDto.class), isNull());
  // }

  // @Test
  // public void testNewMessageWithImage() throws Exception {
  //   // Arrange
  //   MockMultipartFile imageFile = new MockMultipartFile(
  //       "image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

  //   String imageFileName = "uploaded-image.jpg";
  //   when(imageService.uploadImage(any())).thenReturn(imageFileName);

  //   AssistantMessageResponseDto expectedResponse = AssistantMessageResponseDto.builder()
  //       .message("AI response to image")
  //       .build();

  //   when(aiService.sendNewMessage(any(NewMessageRequestDto.class), eq(imageFileName))).thenReturn(expectedResponse);

  //   // Act & Assert
  //   MockMultipartFile messageFile = new MockMultipartFile(
  //       "message", "", "text/plain", "Check this image".getBytes());

  //   MockMultipartFile chatIdFile = new MockMultipartFile(
  //       "chatId", "", "text/plain", testChatId.toString().getBytes());

  //   mockMvc.perform(multipart("/api/chat/send-message")
  //       .file(messageFile)
  //       .file(chatIdFile)
  //       .file(imageFile))
  //       .andExpect(status().isOk())
  //       .andExpect(jsonPath("$.message").value("AI response to image"));

  //   verify(imageService, times(1)).uploadImage(any());
  //   verify(aiService, times(1)).sendNewMessage(any(NewMessageRequestDto.class), eq(imageFileName));
  // }

  // @Test
  // public void testDeleteChat() throws Exception {
  //   // Act & Assert
  //   mockMvc.perform(delete("/api/chat/{id}/delete", testChatId))
  //       .andExpect(status().isNoContent());

  //   verify(messagesService, times(1)).deleteAllByChat(testChatId);
  //   verify(chatService, times(1)).deleteChat(testChatId);
  // }

  // @Test
  // public void testHandleException() throws Exception {
  //   // Arrange
  //   ResponseStatusException exception = new ResponseStatusException(
  //       HttpStatus.BAD_REQUEST, "Invalid request");

  //   when(chatService.getAllChats()).thenThrow(exception);

  //   // Act & Assert
  //   mockMvc.perform(get("/api/chat/all"))
  //       .andExpect(status().isBadRequest())
  //       .andExpect(jsonPath("$.statusCode.value").value(400))
  //       .andExpect(jsonPath("$.message").value(exception.getMessage()));
  // }

  // @Test
  // public void testGetChatHistoryWithInvalidId() throws Exception {
  //   // Arrange
  //   UUID invalidId = UUID.randomUUID();
  //   Pageable pageable = PageRequest.of(0, 10);

  //   when(aiService.getPreviousMessages(invalidId, pageable))
  //       .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

  //   // Act & Assert
  //   mockMvc.perform(get("/api/chat/{id}/messages", invalidId))
  //       .andExpect(status().isNotFound())
  //       .andExpect(jsonPath("$.statusCode.value").value(404))
  //       .andExpect(jsonPath("$.message").value("Chat not found"));
  // }

  // @Test
  // public void testNewMessageWithInvalidRequest() throws Exception {
  //   // Arrange - missing required fields

  //   // Act & Assert
  //   mockMvc.perform(multipart("/api/chat/send-message"))
  //       .andExpect(status().isBadRequest());
  // }

  // @Test
  // public void testGetChatHistoryWithPagination() throws Exception {
  //   // Arrange
  //   Pageable pageable = PageRequest.of(2, 5); // Page 2 with 5 items per page
  //   List<MessageDto> messages = new ArrayList<>();
  //   messages.add(MessageDto.builder().id(UUID.randomUUID()).content("Paged message").build());

  //   HistoryChatDto expectedResponse = HistoryChatDto.builder()
  //       .messages(messages)
  //       .chatId(testChatId)
  //       .build();

  //   when(aiService.getPreviousMessages(eq(testChatId), any(Pageable.class))).thenReturn(expectedResponse);

  //   // Act & Assert
  //   mockMvc.perform(get("/api/chat/{id}/messages", testChatId)
  //       .param("page", "2")
  //       .param("size", "5"))
  //       .andExpect(status().isOk())
  //       .andExpect(jsonPath("$.chatId").value(testChatId.toString()))
  //       .andExpect(jsonPath("$.messages[0].content").value("Paged message"));

  //   // We can't verify exact pageable because of how MockMvc works, but we can
  //   // verify the method was called
  //   verify(aiService, times(1)).getPreviousMessages(eq(testChatId), any(Pageable.class));
  // }
}