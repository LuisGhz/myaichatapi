package dev.luisghtz.myaichat.ai.utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.stereotype.Component;

import dev.luisghtz.myaichat.ai.config.GoogleSearchClient;
import dev.luisghtz.myaichat.chat.entities.Chat;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatClientToolsUtil {
  private final GoogleSearchClient googleSearchClient;

  public ChatClientRequestSpec getChatClientRequestSpec(ChatClient client, Chat chat) {
    var temp = client.prompt();
    if (chat.getIsWebSearchMode()) {
      temp = temp.tools(googleSearchClient).toolNames("google_search");
    }
    return temp;
  }
}
