package dev.luisghtz.myaichat.prompts.services;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.entities.PromptParam;
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

  @Transactional
  public CustomPrompt create(CreateCustomPromptDtoReq createCustomPromptDtoReq) {
    var newCustomPrompt = CustomPrompt.builder()
        .name(createCustomPromptDtoReq.getName())
        .content(createCustomPromptDtoReq.getContent())
        .createdAt(new Date())
        .build();
    addPromptMessagesIfExists(createCustomPromptDtoReq, newCustomPrompt);
    addPromptParamsIfExists(createCustomPromptDtoReq, newCustomPrompt);
    return promptRepository.save(newCustomPrompt);
  }

  private void addPromptMessagesIfExists(CreateCustomPromptDtoReq createCustomPromptDtoReq,
      CustomPrompt customPrompt) {
    if (createCustomPromptDtoReq.getMessages() != null && !createCustomPromptDtoReq.getMessages().isEmpty()) {
      var messages = createCustomPromptDtoReq.getMessages().stream().map(msg -> {
        return PromptMessage.builder()
            .role(msg.getRole())
            .content(msg.getContent())
            .prompt(customPrompt)
            .build();
      }).toList();

      customPrompt.setMessages(messages);
    }
  }

  private void addPromptParamsIfExists(CreateCustomPromptDtoReq createCustomPromptDtoReq, CustomPrompt customPrompt) {
    if (createCustomPromptDtoReq.getParams() != null && !createCustomPromptDtoReq.getParams().isEmpty()) {
      var params = createCustomPromptDtoReq.getParams().stream().map(param -> {
        return PromptParam.builder()
            .name(param.getName())
            .value(param.getValue())
            .prompt(customPrompt)
            .build();
      }).toList();

      customPrompt.setParams(params);
    }
  }
}
