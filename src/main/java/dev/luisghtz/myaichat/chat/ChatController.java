package dev.luisghtz.myaichat.chat;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.auth.annotation.UserJwtData;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.chat.dtos.AssistantMessageResponseDto;
import dev.luisghtz.myaichat.chat.dtos.ChangeIsWebSearchModeReqDto;
import dev.luisghtz.myaichat.chat.dtos.ChangeMaxOutputTokensReqDto;
import dev.luisghtz.myaichat.chat.dtos.ChatsListResponseDto;
import dev.luisghtz.myaichat.chat.dtos.HistoryChatDto;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;
import dev.luisghtz.myaichat.chat.dtos.RenameChatTitleDto;
import dev.luisghtz.myaichat.chat.services.MessagesService;
import dev.luisghtz.myaichat.chat.services.ChatService;
import dev.luisghtz.myaichat.file.FileService;
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
  private final FileService fileService;
  private final ChatService chatService;

  @GetMapping("all")
  public ResponseEntity<ChatsListResponseDto> getChatsList(
      @UserJwtData UserJwtDataDto user) {
    var chats = chatService.getAllChats(user.getId());
    return ResponseEntity.ok(chats);
  }

  @GetMapping("{id}/messages")
  public ResponseEntity<HistoryChatDto> getChatHistory(@PathVariable UUID id,
      @PageableDefault(size = 10) Pageable pageable, @UserJwtData UserJwtDataDto user) {
    return ResponseEntity.ok(messagesService.getPreviousMessages(id, pageable, user));
  }

  @PostMapping("send-message")
  public ResponseEntity<AssistantMessageResponseDto> newMessage(
      @Validated @ModelAttribute NewMessageRequestDto newMessageRequestDto,
      @UserJwtData UserJwtDataDto user) {
    String fileName = null;
    if (newMessageRequestDto.getFile() != null) {
      fileName = fileService.uploadFile(newMessageRequestDto.getFile());
    }
    var response = messagesService.sendNewMessage(newMessageRequestDto, fileName, user);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("{id}/delete")
  public ResponseEntity<Void> deleteChat(@PathVariable UUID id, @UserJwtData UserJwtDataDto user) {
    messagesService.deleteAllByChat(id, user);
    chatService.deleteChat(id, user);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/rename")
  public ResponseEntity<Void> renameChat(@PathVariable UUID id,
      @Validated @RequestBody RenameChatTitleDto renameChatTitleDto, @UserJwtData UserJwtDataDto user) {
    chatService.renameChatTitleById(id, renameChatTitleDto.getTitle(), user);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/change-max-output-tokens")
  public ResponseEntity<Void> changeMaxOutputTokens(@PathVariable UUID id,
      @Validated @RequestBody ChangeMaxOutputTokensReqDto changeMaxOutputTokensReqDto,
      @UserJwtData UserJwtDataDto user) {
    chatService.changeMaxOutputTokens(id, changeMaxOutputTokensReqDto.getMaxOutputTokens(), user);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/change-web-search-mode")
  public ResponseEntity<Void> changeIsWebSearchMode(@PathVariable UUID id,
      @Validated @RequestBody ChangeIsWebSearchModeReqDto changeIsWebSearchMode, @UserJwtData UserJwtDataDto user) {
    chatService.changeIsWebSearchMode(id, changeIsWebSearchMode.getIsWebSearchMode(), user);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{id}/toggle-chat-fav")
  public ResponseEntity<Void> toggleChatFav(@PathVariable UUID id, @UserJwtData UserJwtDataDto user) {
    chatService.toggleChatFav(id, user);
    return ResponseEntity.noContent().build();
  }

}
