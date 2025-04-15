package dev.luisghtz.myaichat.prompts.services;

import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
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
}
