package dev.luisghtz.myaichat.ai.utils;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;

import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;

public class MessagesUtil {
  public static void addSystemMessage(Chat chat, List<Message> messages) {
    if (chat.getCustomPrompt() != null) {
      CustomPrompt customPrompt = chat.getCustomPrompt();
      var promptTemplate = new PromptTemplate(customPrompt.getContent());
      if (customPrompt.getParams() != null && !customPrompt.getParams().isEmpty()) {
        customPrompt.getParams().forEach((param) -> {
          promptTemplate.add(param.getName(), param.getValue());
        });
      }
      messages.add(new SystemMessage(promptTemplate.render()));
      return;
    }

    messages.add(new SystemMessage("You are a helpful assistant."));
  }

  public static void addInitialMessagesIfApply(Chat chat, List<Message> messages) {
    if (chat.getCustomPrompt() != null && chat.getCustomPrompt().getMessages() != null
        && !chat.getCustomPrompt().getMessages().isEmpty()) {
      CustomPrompt customPrompt = chat.getCustomPrompt();
      customPrompt.getMessages().forEach((message) -> {
        if (message.getRole().equals("Assistant")) {
          messages.add(new AssistantMessage(message.getContent()));
        } else if (message.getRole().equals("User")) {
          messages.add(new UserMessage(message.getContent()));
        }
      });
    }
  }
}
