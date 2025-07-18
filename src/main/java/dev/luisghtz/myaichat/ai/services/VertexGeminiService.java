package dev.luisghtz.myaichat.ai.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import dev.luisghtz.myaichat.ai.models.AIProviderService;
import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.utils.MessagesUtil;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.FileNotValidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service("vertexGeminiService")
@RequiredArgsConstructor
@Log4j2
public class VertexGeminiService implements AIProviderService {
  private final ChatClient vertextAIChatClient;
  private final String TITLE_PROMPT = "Generate a concise title of no more than 5 words that summarizes this conversation, "
      + "avoid to use markdown styles, title should be only text. "
      + "The title should be in the same language as the conversation.";

  @Override
  public ChatResponse sendNewMessage(List<AppMessage> messages, Chat chat) {
    List<Message> modelMessages = new ArrayList<>();
    MessagesUtil.addSystemMessage(chat, modelMessages);
    MessagesUtil.addInitialMessagesIfApply(chat, modelMessages);

    // Convert AppMessages to the appropriate Message type
    List<Message> convertedMessages = messages.stream().map(message -> {
      if (message.getRole().equals("Assistant"))
        return new AssistantMessage(message.getContent());
      return generateUserMessage(message);
    }).collect(Collectors.toList());

    modelMessages.addAll(convertedMessages);
    // Always create the chat response
    VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
        .model(chat.getModel())
        .maxOutputTokens(chat.getMaxOutputTokens().intValue())
        .googleSearchRetrieval(chat.getIsWebSearchMode())
        .build();
    ChatResponse chatResponse = vertextAIChatClient.prompt()
        .messages(modelMessages).options(options).call().chatResponse();

    return chatResponse;
  }

  @Override
  public String generateTitle(String userMessage, String assistantMessage) {
    final int MAX_TOKENS = 50;
    List<Message> titleMessages = new ArrayList<>();
    titleMessages.add(new UserMessage(userMessage));
    titleMessages.add(new AssistantMessage(assistantMessage));
    titleMessages
        .add(new UserMessage(
            TITLE_PROMPT));

    VertexAiGeminiChatOptions titleOptions = VertexAiGeminiChatOptions.builder()
        .model(AppModels.GEMINI_FLASH_2_0_LITE.getKey())
        .maxOutputTokens(MAX_TOKENS)
        .build();

    ChatResponse titleResponse = vertextAIChatClient.prompt()
        .messages(titleMessages).options(titleOptions).call().chatResponse();
    String title = titleResponse.getResult().getOutput().getText();
    log.debug("Title response: {}", title);
    return title;
  }

  private Message generateUserMessage(AppMessage message) {
    if (message.getFileUrl() != null &&
        message.getId() == null) {
      try {
        MimeType mimeType = getMimeType(message.getFileUrl());
        return new UserMessage(message.getContent(),
            new Media(mimeType, new URL(message.getFileUrl())));
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    return new UserMessage(message.getContent());
  }

  private MimeType getMimeType(String fileUrl) {
    if (fileUrl.endsWith(".gif")) {
      return MimeTypeUtils.IMAGE_GIF;
    } else if (fileUrl.endsWith(".png")) {
      return MimeTypeUtils.IMAGE_PNG;
    } else if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".jpeg")) {
      return MimeTypeUtils.IMAGE_JPEG;
    } else {
      throw new FileNotValidException("File not valid. Supported formats: gif, png, jpg, jpeg.");
    }
  }
}
