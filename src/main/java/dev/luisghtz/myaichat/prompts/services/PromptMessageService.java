package dev.luisghtz.myaichat.prompts.services;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.repositories.PromptMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptMessageService {
  private final PromptMessageRepository promptMessageRepository;

  public void deleteByIdAndPromptId(String id, String promptId) {
    promptMessageRepository.deleteByIdAndPromptId(UUID.fromString(id), UUID.fromString(promptId));
  }

  public void addPromptMessagesIfExists(CreateCustomPromptDtoReq createCustomPromptDtoReq,
      CustomPrompt customPrompt) {
    if (createCustomPromptDtoReq.getMessages() != null && !createCustomPromptDtoReq.getMessages().isEmpty()) {
      var messages = createCustomPromptDtoReq.getMessages().stream()
          .map(msg -> {
            handleInvalidMessage(msg);
            return PromptMessage.builder()
                .role(msg.getRole())
                .content(msg.getContent())
                .prompt(customPrompt)
                .build();
          }).toList();

      customPrompt.setMessages(messages);
    }
  }

  public void addPromptMessagesIfExistsToExistingPrompt(UpdateCustomPromptDtoReq updateCustomPromptDtoReq,
      CustomPrompt customPrompt) {
    if (updateCustomPromptDtoReq.getMessages() != null && !updateCustomPromptDtoReq.getMessages().isEmpty()) {
      if (customPrompt.getMessages() == null)
        customPrompt.setMessages(new ArrayList<>());
      var messages = updateCustomPromptDtoReq.getMessages().stream()
          .filter(msg -> msg.getId() == null || msg.getId().toString().isEmpty())
          .map(msg -> {
            handleInvalidMessage(msg);

            return PromptMessage.builder()
                .role(msg.getRole())
                .content(msg.getContent())
                .prompt(customPrompt)
                .build();
          }).toList();

      if (!messages.isEmpty())
        customPrompt.getMessages().addAll(messages);
    }
  }

  public void handleUpdatedExistingMessages(
      UpdateCustomPromptDtoReq createCustomPromptDtoReq, CustomPrompt customPrompt) {
    if (createCustomPromptDtoReq.getMessages() != null && !createCustomPromptDtoReq.getMessages().isEmpty()) {
      createCustomPromptDtoReq.getMessages().stream()
          .filter(msg -> msg.getId() != null && !msg.getId().toString().isEmpty())
          .forEach(msg -> {
            customPrompt.getMessages().stream()
                .filter(m -> m.getId().toString().equals(msg.getId().toString()))
                .findFirst()
                .ifPresent(existingMessage -> {
                  if (msg.getRole() != null) {
                    existingMessage.setRole(msg.getRole());
                  }
                  if (msg.getContent() != null) {
                    existingMessage.setContent(msg.getContent());
                  }
                });
          });
    }
  }

  private <T> void handleInvalidMessage(T message) {
    String role = null;
    String content = null;
    
    if (message instanceof UpdateCustomPromptMessagesDto updateDto) {
      role = updateDto.getRole();
      content = updateDto.getContent();
    } else if (message instanceof CreateCustomPromptMessagesDto createDto) {
      role = createDto.getRole();
      content = createDto.getContent();
    }
    
    if (role == null || role.trim().isEmpty())
      throw new AppMethodArgumentNotValidException("Message role cannot be null or empty");
    if (content == null || content.trim().isEmpty())
      throw new AppMethodArgumentNotValidException("Message content cannot be null or empty");
  }
}
