package dev.luisghtz.myaichat.chat;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.dtos.RenameChatTitleDto;
import dev.luisghtz.myaichat.chat.services.MessagesService;
import dev.luisghtz.myaichat.chat.services.ChatService;
import dev.luisghtz.myaichat.image.ImageService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
@Log4j2
public class ChatController {
  private final MessagesService messagesService;
  private final ImageService imageService;
  private final ChatService chatService;

  @GetMapping("all")
  public ResponseEntity<ChatsListResponseDto> getChatsList() {
    var chats = chatService.getAllChats();
    return ResponseEntity.ok(chats);
  }

  @GetMapping("{id}/messages")
  public ResponseEntity<HistoryChatDto> getChatHistory(@PathVariable UUID id,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(messagesService.getPreviousMessages(id, pageable));
  }

  @PostMapping("send-message")
  public ResponseEntity<AssistantMessageResponseDto> newMessage(
      @Validated @ModelAttribute NewMessageRequestDto newMessageRequestDto) {
    String imageFileName = null;
    if (newMessageRequestDto.getImage() != null) {
      imageFileName = imageService.uploadImage(newMessageRequestDto.getImage());
    }
    var response = messagesService.sendNewMessage(newMessageRequestDto, imageFileName);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("{id}/delete")
  public ResponseEntity<Void> deleteChat(@PathVariable UUID id) {
    messagesService.deleteAllByChat(id);
    chatService.deleteChat(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/rename")
  public ResponseEntity<Void> renameChat(@PathVariable UUID id,
      @Validated @RequestBody RenameChatTitleDto renameChatTitleDto) {
    chatService.renameChatTitleById(id, renameChatTitleDto.getTitle());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/change-max-output-tokens")
  public ResponseEntity<Void> changeMaxOutputTokens(@PathVariable UUID id,
      @Validated @RequestBody NewMessageRequestDto newMessageRequestDto) {
    chatService.changeMaxOutputTokens(id, newMessageRequestDto.getMaxOutputTokens());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/toggle-chat-fav")
  public ResponseEntity<Void> toggleChatFav(@PathVariable UUID id) {
    chatService.toggleChatFav(id);
    return ResponseEntity.noContent().build();
  }

}
