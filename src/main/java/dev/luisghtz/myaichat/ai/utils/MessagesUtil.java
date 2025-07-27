package dev.luisghtz.myaichat.ai.utils;

import java.time.LocalDate;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;

public class MessagesUtil {
  public static void addSystemMessage(Chat chat, List<Message> messages) {
    if (chat.getCustomPrompt() != null) {
      CustomPrompt customPrompt = chat.getCustomPrompt();
      var prompt = addWebSearchInstructionsIfApply(chat, customPrompt.getContent());
      messages.add(new SystemMessage(prompt));
      return;
    }
    var prompt = addWebSearchInstructionsIfApply(chat, "You are a helpful assistant.");
    messages.add(new SystemMessage(prompt));
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

  private static String addWebSearchInstructionsIfApply(Chat chat, String prompt) {
    var currentDate = LocalDate.now();
    var year = currentDate.getYear();
    if (chat.getIsWebSearchMode()) {
      var webSearchInstructions = String.format(
          """
              --- WEB SEARCH INSTRUCTIONS ---
              When you use "web_search" you can search until %s so avoid to search with your cutoff date, for instance search "President of USA %d" instead of "President of USA 2023" where 2023 is the cutoff year for some models.
              """,
          currentDate, year);
      return prompt += webSearchInstructions;
    }

    return prompt;
  }
}
