package dev.luisghtz.myaichat.prompts;

import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.auth.annotation.UserJwtData;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.auth.services.UserService;

import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdatedCustomPromptDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@RestController
@RequestMapping("api/custom-prompts")
@RequiredArgsConstructor
public class PromptsController {
  private final CustomPromptService customPromptService;
  private final UserService userService;

  @GetMapping("/all")
  public ResponseEntity<PromptsListDtoRes> getAllPrompts(@UserJwtData UserJwtDataDto userJwtData) {
    return ResponseEntity.ok(customPromptService.findAllByUserId(UUID.fromString(userJwtData.getId())));
  }

  @GetMapping("/{promptId}")
  public ResponseEntity<CustomPrompt> getPromptById(@PathVariable String promptId,
      @UserJwtData UserJwtDataDto userJwtData) {
    return customPromptService.findByIdAndUserId(promptId, UUID.fromString(userJwtData.getId()))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping()
  public ResponseEntity<CustomPrompt> create(
      @Valid @RequestBody CreateCustomPromptDtoReq createCustomPromptDtoReq,
      @UserJwtData UserJwtDataDto userJwtData) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(customPromptService.create(createCustomPromptDtoReq, userJwtData.getId()));
  }

  @PatchMapping("/{promptId}/update")
  public ResponseEntity<UpdatedCustomPromptDtoRes> update(
      @PathVariable UUID promptId,
      @Valid @RequestBody UpdateCustomPromptDtoReq updateCustomPromptDtoReq,
      @UserJwtData UserJwtDataDto userJwtData) {
    customPromptService.update(promptId.toString(), updateCustomPromptDtoReq,
        UUID.fromString(userJwtData.getId()));
    var res = new UpdatedCustomPromptDtoRes("Prompt updated successfully");
    return ResponseEntity.ok(res);
  }

  @DeleteMapping("/{promptId}/{messageId}/delete-message")
  public ResponseEntity<String> deleteMessage(
      @PathVariable UUID promptId,
      @PathVariable UUID messageId,
      @UserJwtData UserJwtDataDto userJwtData) throws Exception {
    customPromptService.deleteMessage(promptId.toString(), messageId.toString(),
        UUID.fromString(userJwtData.getId()));
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{promptId}/delete")
  public ResponseEntity<Void> deletePrompt(@PathVariable UUID promptId,
      @UserJwtData UserJwtDataDto userJwtData) {
    customPromptService.delete(promptId.toString(), UUID.fromString(userJwtData.getId()));
    return ResponseEntity.ok().build();
  }

}
