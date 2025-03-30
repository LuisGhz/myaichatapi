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
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAIService {
  private final ChatModel chatModel;

  public ChatResponse sendNewMessage(List<AppMessage> messages, String model) {
    List<Message> modelMessages = new ArrayList<>();
    modelMessages.add(new SystemMessage("You are an intelligent assistant."));
    
    // Convert AppMessages to the appropriate Message type
    List<Message> convertedMessages = messages.stream().map(message -> {
      if (message.getRole().equals("Assistant"))
        return new AssistantMessage(message.getContent());
      
      return new UserMessage(message.getContent());
    }).collect(Collectors.toList());
    
    modelMessages.addAll(convertedMessages);    
    // Always create the chat response
    OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model(model)
        .maxCompletionTokens(1000)
        .build();

    ChatResponse chatResponse = chatModel.call(new Prompt(modelMessages, options));
    
    return chatResponse;
  }

  public String generateTitle(Chat chat, String userMessage, String assistantMessage) {
    var MAX_COMPLETION_TOKENS = 50;
    List<Message> titleMessages = new ArrayList<>();
    titleMessages
        .add(new SystemMessage("Generate a concise title of no more than 10 words that summarizes this conversation."));
    titleMessages.add(new UserMessage(userMessage));
    titleMessages.add(new AssistantMessage(assistantMessage));

    OpenAiChatOptions titleOptions = OpenAiChatOptions.builder()
        .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
        .maxCompletionTokens(MAX_COMPLETION_TOKENS)
        .build();

    ChatResponse titleResponse = chatModel.call(new Prompt(titleMessages, titleOptions));

    return titleResponse.getResult().getOutput().getText();
  }
}
