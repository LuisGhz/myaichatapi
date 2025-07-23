package dev.luisghtz.myaichat.ai.utils;

import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MessagesUtilTest {

  private Chat chat;
  private List<Message> messages;

  @BeforeEach
  void setUp() {
    chat = new Chat(); // Assuming Chat has a default constructor
    messages = new ArrayList<>();
  }

  @Nested
  @DisplayName("addSystemMessage Tests")
  class AddSystemMessageTests {

    @Test
    @DisplayName("should add default system message when chat has no custom prompt")
    void addSystemMessage_noCustomPrompt_addsDefault() {
      chat.setCustomPrompt(null); // Ensure customPrompt is null

      MessagesUtil.addSystemMessage(chat, messages);

      assertEquals(1, messages.size());
      assertTrue(messages.get(0) instanceof SystemMessage);
      assertEquals("You are a helpful assistant.", messages.get(0).getText());
    }

    @Test
    @DisplayName("Should add custom system message without params")
    void addSystemMessage_customPromptNoParams_addsCustom() {
      CustomPrompt customPrompt = new CustomPrompt();
      customPrompt.setContent("This is a custom system prompt.");
      chat.setCustomPrompt(customPrompt);

      MessagesUtil.addSystemMessage(chat, messages);

      assertEquals(1, messages.size());
      assertTrue(messages.get(0) instanceof SystemMessage);
      assertEquals("This is a custom system prompt.", messages.get(0).getText());
    }
  }

  @Nested
  @DisplayName("addInitialMessagesIfApply Tests")
  class AddInitialMessagesIfApplyTests {

    @Test
    @DisplayName("should add initial messages from custom prompt")
    void addInitialMessages_customPrompt_addsInitialMessages() {
      CustomPrompt customPrompt = new CustomPrompt();
      customPrompt.setMessages(List.of(
          PromptMessage.builder().role("User").content("Hello!").build(),
          PromptMessage.builder().role("Assistant").content("Hi there!").build()
      ));
      chat.setCustomPrompt(customPrompt);

      MessagesUtil.addInitialMessagesIfApply(chat, messages);

      assertEquals(2, messages.size());
      assertTrue(messages.get(0) instanceof UserMessage);
      assertEquals("Hello!", messages.get(0).getText());
      assertTrue(messages.get(1) instanceof AssistantMessage);
      assertEquals("Hi there!", messages.get(1).getText());
    }

    @Test
    @DisplayName("should not add initial messages if no custom prompt")
    void addInitialMessages_noCustomPrompt_doesNotAdd() {
      chat.setCustomPrompt(null); // Ensure customPrompt is null

      MessagesUtil.addInitialMessagesIfApply(chat, messages);

      assertTrue(messages.isEmpty());
    }
  }
}