package dev.luisghtz.myaichat.prompts;

import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("api/custom-prompts")
@RequiredArgsConstructor
public class PrompsController {
  private final CustomPromptService customPromptService;

  @GetMapping()
  public ResponseEntity<PromptsListDtoRes> getAllPrompts() {
    return ResponseEntity.ok(customPromptService.findAll());
  }

}
