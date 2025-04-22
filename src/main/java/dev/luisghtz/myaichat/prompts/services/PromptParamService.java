package dev.luisghtz.myaichat.prompts.services;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptParam;
import dev.luisghtz.myaichat.prompts.repositories.PromptParamRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptParamService {
  private final PromptParamRepository promptParamRepository;

  public PromptParam save(PromptParam promptParam) {
    return promptParamRepository.save(promptParam);
  }

  public Optional<PromptParam> findById(String string) {
    return promptParamRepository.findById(UUID.fromString(string));
  }

  public void deleteByIdAndPromptId(String id, String promptId) {
    promptParamRepository.deleteByIdAndPromptId(UUID.fromString(id), UUID.fromString(promptId));
  }

  public void addPromptParamsIfExists(CreateCustomPromptDtoReq createCustomPromptDtoReq, CustomPrompt customPrompt) {
    if (createCustomPromptDtoReq.getParams() != null && !createCustomPromptDtoReq.getParams().isEmpty()) {
      if (customPrompt.getParams() == null)
        customPrompt.setParams(new ArrayList<>());
      var params = createCustomPromptDtoReq.getParams().stream()
          .map(param -> {
            handleInvalidNewParamProps(param);
            return PromptParam.builder()
                .name(param.getName())
                .value(param.getValue())
                .prompt(customPrompt)
                .build();
          }).toList();

      customPrompt.setParams(params);
    }
  }

  public void addPromptParamsIfExistsToExistingPrompt(UpdateCustomPromptDtoReq updateCustomPromptDtoReq,
      CustomPrompt customPrompt) {
    if (updateCustomPromptDtoReq.getParams() != null && !updateCustomPromptDtoReq.getParams().isEmpty()) {
      if (customPrompt.getParams() == null)
        customPrompt.setParams(new ArrayList<>());
      var params = updateCustomPromptDtoReq.getParams().stream()
          .filter(param -> param.getId() == null || param.getId().isEmpty())
          .map(param -> {
            handleInvalidNewParamProps(param);
            handleExistingParamName(param, customPrompt);
            return PromptParam.builder()
                .name(param.getName())
                .value(param.getValue())
                .prompt(customPrompt)
                .build();
          }).toList();

      if (!params.isEmpty())
        customPrompt.getParams().addAll(params);
    }
  }

  public void handleUpdatedExistingParams(
      UpdateCustomPromptDtoReq createCustomPromptDtoReq, CustomPrompt customPrompt) {
    if (createCustomPromptDtoReq.getParams() != null && !createCustomPromptDtoReq.getParams().isEmpty()) {
      createCustomPromptDtoReq.getParams().stream()
          .filter(param -> param.getId() != null && !param.getId().isEmpty())
          .forEach(param -> {
            customPrompt.getParams().stream()
                .filter(p -> p.getId().toString().equals(param.getId()))
                .findFirst()
                .ifPresent(existingParam -> {
                  if (param.getName() != null && !param.getName().trim().isEmpty()) {
                    handleInvalidParamNameOnModification(param, customPrompt);
                    existingParam.setName(param.getName());
                  }
                  if (param.getValue() != null && !param.getValue().trim().isEmpty())
                    existingParam.setValue(param.getValue());
                });
          });
    }
  }

  private <T> void handleInvalidNewParamProps(T param) {
    String name = null;
    String value = null;

    if (param instanceof UpdateCustomPromptParamsDto) {
      name = ((UpdateCustomPromptParamsDto) param).getName();
      value = ((UpdateCustomPromptParamsDto) param).getValue();
    } else if (param instanceof CreateCustomPromptParamsDto) {
      name = ((CreateCustomPromptParamsDto) param).getName();
      value = ((CreateCustomPromptParamsDto) param).getValue();
    }

    if (name == null || name.trim().isEmpty())
      throw new AppMethodArgumentNotValidException("Param name cannot be null or empty");
    if (value == null || value.trim().isEmpty())
      throw new AppMethodArgumentNotValidException("Param value cannot be null or empty");
  }

  private void handleInvalidParamNameOnModification(UpdateCustomPromptParamsDto param, CustomPrompt customPrompt) {
    if (customPrompt.getParams().stream()
        .anyMatch(
            p -> p.getName().toLowerCase().equals(param.getName().toLowerCase())
                && !p.getId().toString().equals(param.getId().toString())))
      throw new AppMethodArgumentNotValidException("Param already exists with the same name");
  }

  private void handleExistingParamName(UpdateCustomPromptParamsDto param, CustomPrompt customPrompt) {
    if (customPrompt.getParams().stream()
        .anyMatch(p -> p.getName().toLowerCase().equals(param.getName().toLowerCase())))
      throw new AppMethodArgumentNotValidException("Param already exists with the same name");
  }

}
