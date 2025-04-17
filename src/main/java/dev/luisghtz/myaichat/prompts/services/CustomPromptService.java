package dev.luisghtz.myaichat.prompts.services;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.repositories.CustomRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomPromptService {
  private final CustomRepository promptRepository;
  private final PromptParamService promptParamService;
  private final PromptMessageService promptMessageService;

  public PromptsListDtoRes findAll() {
    var prompts = promptRepository.findAllSummary();
    var promptsDto = new PromptsListDtoRes(prompts);
    return promptsDto;
  }

  public Optional<CustomPrompt> findById(String promptId) {
    return promptRepository.findById(UUID.fromString(promptId));
  }

  @Transactional
  public CustomPrompt create(CreateCustomPromptDtoReq createCustomPromptDtoReq) {
    var newCustomPrompt = CustomPrompt.builder()
        .name(createCustomPromptDtoReq.getName())
        .content(createCustomPromptDtoReq.getContent())
        .createdAt(new Date())
        .build();
    promptMessageService.addPromptMessagesIfExists(createCustomPromptDtoReq, newCustomPrompt);
    promptParamService.addPromptParamsIfExists(createCustomPromptDtoReq, newCustomPrompt);
    return promptRepository.save(newCustomPrompt);
  }

  @Transactional
  public void update(String promptId, UpdateCustomPromptDtoReq updateCustomPromptDtoReq) {
    var customPrompt = findById(promptId).orElseThrow(() -> new AppNotFoundException("Prompt not found"));
    if (updateCustomPromptDtoReq.getName() != null && !updateCustomPromptDtoReq.getName().isEmpty())
      customPrompt.setName(updateCustomPromptDtoReq.getName());
    if (updateCustomPromptDtoReq.getContent() != null && !updateCustomPromptDtoReq.getContent().isEmpty())
      customPrompt.setContent(updateCustomPromptDtoReq.getContent());

    promptMessageService.addPromptMessagesIfExistsToExistingPrompt(updateCustomPromptDtoReq, customPrompt);
    promptParamService.addPromptParamsIfExistsToExistingPrompt(updateCustomPromptDtoReq, customPrompt);

    promptParamService.handleUpdatedExistingParams(updateCustomPromptDtoReq, customPrompt);
    promptMessageService.handleUpdatedExistingMessages(updateCustomPromptDtoReq, customPrompt);

    customPrompt.setUpdatedAt(new Date());
    promptRepository.save(customPrompt);
  }

  @Transactional
  public void deleteParam(String promptId, String paramId) throws Exception {
    var customPrompt = findById(promptId).orElseThrow(() -> new AppNotFoundException("Prompt not found"));
    var param = customPrompt.getParams().stream()
        .filter(p -> p.getId().toString().equals(paramId))
        .findFirst()
        .orElseThrow(() -> new AppNotFoundException("Prompt param not found"));
    customPrompt.getParams().remove(param);
    promptParamService.deleteByIdAndPromptId(paramId, promptId);
    promptRepository.save(customPrompt);
  }

  @Transactional
  public void deleteMessage(String promptId, String messageId) throws Exception {
    var customPrompt = findById(promptId).orElseThrow(() -> new AppNotFoundException("Prompt not found"));
    var message = customPrompt.getMessages().stream()
        .filter(m -> m.getId().toString().equals(messageId))
        .findFirst()
        .orElseThrow(() -> new AppNotFoundException("Prompt message not found"));
    customPrompt.getMessages().remove(message);
    promptMessageService.deleteByIdAndPromptId(messageId, promptId);
    promptRepository.save(customPrompt);
  }

  @Transactional
  public void delete(String promptId) {
    var customPrompt = findById(promptId).orElseThrow(() -> new AppNotFoundException("Prompt not found"));
    promptRepository.delete(customPrompt);
  }
}
