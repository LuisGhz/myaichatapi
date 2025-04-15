package dev.luisghtz.myaichat.prompts;

import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/custom-prompts")
@RequiredArgsConstructor
public class PrompsController {
  private final CustomPromptService customPromptService;

  @GetMapping()
  public ResponseEntity<PromptsListDtoRes> getAllPrompts() {
    return ResponseEntity.ok(customPromptService.findAll());
  }

  @PostMapping()
  public ResponseEntity<CustomPrompt> create(@RequestBody CreateCustomPromptDtoReq createCustomPromptDtoReq)
      throws Exception {
    return ResponseEntity.status(HttpStatus.CREATED).body(customPromptService.create(createCustomPromptDtoReq));
  }

}
