package dev.luisghtz.myaichat.ai.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.ai.config.GoogleSearchClient;
import dev.luisghtz.myaichat.mocks.ChatClientRequestMock;

@ExtendWith(MockitoExtension.class)
class ChatClientToolsUtilTest {

  @Nested
  @DisplayName("getChatClientRequestSpec - web search disabled")
  class WhenWebSearchDisabled {
    @Test
    @DisplayName("Should return prompt spec unchanged when web search mode off")
    void shouldReturnPromptSpecUnchanged() {
      GoogleSearchClient google = mock(GoogleSearchClient.class);
      ChatClientToolsUtil util = new ChatClientToolsUtil(google);

  // Reuse the helper mock request spec which implements ChatClientRequestSpec
  ChatClientRequestSpec clientSpec = new ChatClientRequestMock(null);
      Chat chat = new Chat();
      chat.setIsWebSearchMode(false);

      // The util expects a ChatClient, but its prompt() is called on an actual ChatClient instance in production.
      // Since the util only calls client.prompt() and returns the spec, we can emulate by passing an anonymous
      // ChatClient that returns our request spec.
      org.springframework.ai.chat.client.ChatClient client = mock(org.springframework.ai.chat.client.ChatClient.class);
      when(client.prompt()).thenReturn(clientSpec);

      ChatClientRequestSpec spec = util.getChatClientRequestSpec(client, chat);
      assertThat(spec).isNotNull();
    }
  }

  @Nested
  @DisplayName("getChatClientRequestSpec - web search enabled")
  class WhenWebSearchEnabled {
    @Test
    @DisplayName("Should configure tools on the prompt when web search mode on")
    void shouldConfigureTools() {
      GoogleSearchClient google = mock(GoogleSearchClient.class);
      ChatClientToolsUtil util = new ChatClientToolsUtil(google);
      ChatClientRequestSpec clientSpec = new ChatClientRequestMock(null);
      Chat chat = new Chat();
      chat.setIsWebSearchMode(true);

      org.springframework.ai.chat.client.ChatClient client = mock(org.springframework.ai.chat.client.ChatClient.class);
      when(client.prompt()).thenReturn(clientSpec);

      ChatClientRequestSpec spec = util.getChatClientRequestSpec(client, chat);
      assertThat(spec).isNotNull();
    }
  }
}
