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
import dev.luisghtz.myaichat.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
@Log4j2
public class ChatController {
  private final AIService aiService;
  private final ImageService imageService;

  @GetMapping("all")
  public ResponseEntity<ChatsListResponseDto> getChatsList() {
    var chats = aiService.getAllChats();
    return ResponseEntity.ok(chats);
  }

  @GetMapping("{id}/messages")
  public ResponseEntity<HistoryChatDto> getChatHistory(@PathVariable UUID id,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(aiService.getChatHistory(id, pageable));
  }

  @PostMapping("send-message")
  public ResponseEntity<AssistantMessageResponseDto> newMessage(
      @Validated @ModelAttribute NewMessageRequestDto newMessageRequestDto) {
    String imageFileName = null;
    if (newMessageRequestDto.getImage() != null) {
      imageFileName = imageService.uploadImage(newMessageRequestDto.getImage());
    }
    var response = aiService.sendNewMessage(newMessageRequestDto, imageFileName);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("{id}/delete")
  public ResponseEntity<Void> deleteChat(@PathVariable UUID id) {
    aiService.deleteChat(id);
    return ResponseEntity.noContent().build();
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
