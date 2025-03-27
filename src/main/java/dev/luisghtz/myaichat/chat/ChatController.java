package dev.luisghtz.myaichat.chat;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatErrorResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.services.AIService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
public class ChatController {
  private final AIService aiService;

  @GetMapping("all")
  public ResponseEntity<ChatsListResponseDto> getChatsList() {
    var chats = aiService.getAllChats();
    return ResponseEntity.ok(chats);
  }

  @GetMapping("{id}/messages")
  public ResponseEntity<HistoryChatDto> getChatHistory(@PathVariable UUID id) {
    return ResponseEntity.ok(aiService.getChatHistory(id));
  }

  @PostMapping("send-message")
  public ResponseEntity<AssistantMessageResponseDto> newMessage(
      @RequestBody NewMessageRequestDto newMessageRequestDto) {
    var response = aiService.sendNewMessage(newMessageRequestDto);
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ChatErrorResponseDto> handleException(ResponseStatusException ex) {
    var statusCode = ex.getStatusCode();
    var message = ex.getMessage();
    var response = ChatErrorResponseDto.builder()
        .statusCode(statusCode)
        .message(message)
        .build();
    return new ResponseEntity<>(response, statusCode);
  }

}
