package dev.luisghtz.myaichat.prompts.services;

import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.repositories.CustomRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomPromptService {
  private final CustomRepository promptRepository;

  public PromptsListDtoRes findAll() {
    var prompts = promptRepository.findAll();
    var promptsDto = new PromptsListDtoRes(prompts);
    return promptsDto;
  }

  public CustomPrompt create(CreateCustomPromptDtoReq createCustomPromptDtoReq) {
    var newCustomPrompt = CustomPrompt.builder()
        .name(createCustomPromptDtoReq.getName())
        .model(createCustomPromptDtoReq.getModel())
        .systemMessage(createCustomPromptDtoReq.getSystemMessage())
        .build();
    return promptRepository.save(newCustomPrompt);
  }
}
