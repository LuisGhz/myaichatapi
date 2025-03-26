package dev.luisghtz.myaichat.chat.services;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OpenAIService {
  private final ChatModel chatModel;

  public ChatResponse sendNewMessage(List<AppMessage> messages, String model) {
    List<Message> modelMessages = new ArrayList<>();
    modelMessages.add(new SystemMessage("You are an intelligent assistant."));
    modelMessages.addAll(messages.stream().map(message -> {
      log.info(message.getContent());
      if (message.getRole().equals("Assistant"))
        return new AssistantMessage(message.getContent());

      return new UserMessage(message.getContent());
    }).collect(Collectors.toList()));

    ChatResponse chatResponse = chatModel.call(new Prompt(modelMessages,
        OpenAiChatOptions.builder()
            .model(model)
            .maxCompletionTokens(1000)
            .N(1)
            .build()));

    return chatResponse;
  }
}
