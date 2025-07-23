package dev.luisghtz.myaichat.prompts.services;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.luisghtz.myaichat.auth.services.UserService;
import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.exceptions.ResourceInUseException;
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
  private final UserService userService;

  public PromptsListDtoRes findAll() {
    var prompts = promptRepository.findAllSummary();
    var promptsDto = new PromptsListDtoRes(prompts);
    return promptsDto;
  }

  public PromptsListDtoRes findAllByUserId(UUID userId) {
    var prompts = promptRepository.findAllSummaryByUserId(userId);
    var promptsDto = new PromptsListDtoRes(prompts);
    return promptsDto;
  }

  public Optional<CustomPrompt> findById(String promptId) {
    return promptRepository.findById(UUID.fromString(promptId));
  }

  public Optional<CustomPrompt> findByIdAndUserId(String promptId, UUID userId) {
    return promptRepository.findByIdAndUserId(UUID.fromString(promptId), userId);
  }

  @Transactional
  public CustomPrompt create(CreateCustomPromptDtoReq createCustomPromptDtoReq, String userId) {
    var user = userService.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    var newCustomPrompt = CustomPrompt.builder()
        .name(createCustomPromptDtoReq.getName())
        .content(createCustomPromptDtoReq.getContent())
        .user(user)
        .createdAt(new Date())
        .build();
    promptMessageService.addPromptMessagesIfExists(createCustomPromptDtoReq, newCustomPrompt);
    promptParamService.addPromptParamsIfExists(createCustomPromptDtoReq, newCustomPrompt);
    return promptRepository.save(newCustomPrompt);
  }

  @Transactional
  public void update(String promptId, UpdateCustomPromptDtoReq updateCustomPromptDtoReq, UUID userId) {
    var customPrompt = getCustomPromptByIdAndUserId(promptId, userId);

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
  public void deleteParam(String promptId, String paramId, UUID userId) throws Exception {
    var customPrompt = getCustomPromptByIdAndUserId(promptId, userId);
    var param = customPrompt.getParams().stream()
        .filter(p -> p.getId().toString().equals(paramId))
        .findFirst()
        .orElseThrow(() -> new AppNotFoundException("Prompt param not found"));
    customPrompt.getParams().remove(param);
    promptParamService.deleteByIdAndPromptId(paramId, promptId);
    promptRepository.save(customPrompt);
  }

  @Transactional
  public void deleteMessage(String promptId, String messageId, UUID userId) throws Exception {
    var customPrompt = getCustomPromptByIdAndUserId(promptId, userId);
    var message = customPrompt.getMessages().stream()
        .filter(m -> m.getId().toString().equals(messageId))
        .findFirst()
        .orElseThrow(() -> new AppNotFoundException("Prompt message not found"));
    customPrompt.getMessages().remove(message);
    promptMessageService.deleteByIdAndPromptId(messageId, promptId);
    promptRepository.save(customPrompt);
  }

  @Transactional
  public void delete(String promptId, UUID userId) {
    var customPrompt = getCustomPromptByIdAndUserId(promptId, userId);
    if (promptRepository.hasAssociatedChats(java.util.UUID.fromString(promptId))) {
      throw new ResourceInUseException("Prompt is in use by a chat, cannot be deleted");
    }
    promptRepository.delete(customPrompt);
  }

  private CustomPrompt getCustomPromptByIdAndUserId(String promptId, UUID userId) {
    return findByIdAndUserId(promptId, userId)
        .orElseThrow(() -> new AppNotFoundException("Prompt not found or you don't have permission to access it"));
  }
}
