package dev.luisghtz.myaichat.prompts;

import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
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

@RestController
@RequestMapping("api/custom-prompts")
@RequiredArgsConstructor
public class PromptsController {
  private final CustomPromptService customPromptService;

  @GetMapping("/all")
  public ResponseEntity<PromptsListDtoRes> getAllPrompts() {
    return ResponseEntity.ok(customPromptService.findAll());
  }

  @GetMapping("/{promptId}")
  public ResponseEntity<CustomPrompt> getPromptById(@PathVariable String promptId) {
    return customPromptService.findById(promptId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping()
  public ResponseEntity<CustomPrompt> create(
      @Valid @RequestBody CreateCustomPromptDtoReq createCustomPromptDtoReq) {
    return ResponseEntity.status(HttpStatus.CREATED).body(customPromptService.create(createCustomPromptDtoReq));
  }

  @PatchMapping("/{promptId}/update")
  public ResponseEntity<String> update(
      @PathVariable String promptId,
      @Valid @RequestBody UpdateCustomPromptDtoReq updateCustomPromptDtoReq) {
    customPromptService.update(promptId, updateCustomPromptDtoReq);
    return ResponseEntity.ok("Prompt updated successfully");
  }

  @DeleteMapping("/{promptId}/{paramId}/delete-param")
  public ResponseEntity<Void> deleteParam(
      @PathVariable String promptId,
      @PathVariable String paramId) throws Exception {
    customPromptService.deleteParam(promptId, paramId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{promptId}/{messageId}/delete-message")
  public ResponseEntity<String> deleteMessage(
      @PathVariable String promptId,
      @PathVariable String messageId) throws Exception {
    customPromptService.deleteMessage(promptId, messageId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{promptId}/delete")
  public ResponseEntity<Void> deletePrompt(@PathVariable String promptId) {
    customPromptService.delete(promptId);
    return ResponseEntity.ok().build();
  }

}
