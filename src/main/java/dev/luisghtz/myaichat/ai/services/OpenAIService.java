package dev.luisghtz.myaichat.ai.services;

import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import dev.luisghtz.myaichat.ai.models.AIProviderService;
import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.utils.MessagesUtil;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.ImageNotValidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service("openAIService")
@RequiredArgsConstructor
@Log4j2
public class OpenAIService implements AIProviderService {
  private final ChatClient openAIChatClient;
  private final String TITLE_PROMPT = "Generate a concise title of no more than 5 words that summarizes this conversation, "
      + "avoid to use markdown styles, title should be only text. "
      + "The title should be in the same language as the conversation.";

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
    OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model(chat.getModel())
        .maxCompletionTokens(AppModels.getMaxTokens(chat.getModel()))
        .build();
    ChatResponse chatResponse = openAIChatClient.prompt()
        .messages(modelMessages).options(options).call().chatResponse();

    return chatResponse;
  }

  public String generateTitle(String userMessage, String assistantMessage) {
    var MAX_COMPLETION_TOKENS = 50;
    List<Message> titleMessages = new ArrayList<>();
    titleMessages
        .add(new SystemMessage(
            TITLE_PROMPT));
    titleMessages.add(new UserMessage(userMessage));
    titleMessages.add(new AssistantMessage(assistantMessage));

    OpenAiChatOptions titleOptions = OpenAiChatOptions.builder()
        .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
        .maxCompletionTokens(MAX_COMPLETION_TOKENS)
        .build();

    ChatResponse titleResponse = openAIChatClient.prompt().messages(titleMessages)
        .options(titleOptions).call().chatResponse();

    return titleResponse.getResult().getOutput().getText();
  }

  private Message generateUserMessage(AppMessage message) {
    if (message.getImageUrl() != null &&
        message.getId() == null) {
      try {
        MimeType mimeType = getMimeType(message.getImageUrl());
        return new UserMessage(message.getContent(),
            new Media(mimeType, new URL(message.getImageUrl())));
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    return new UserMessage(message.getContent());
  }

  private MimeType getMimeType(String imageUrl) {
    if (imageUrl.endsWith(".gif")) {
      return MimeTypeUtils.IMAGE_GIF;
    } else if (imageUrl.endsWith(".png")) {
      return MimeTypeUtils.IMAGE_PNG;
    } else if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg")) {
      return MimeTypeUtils.IMAGE_JPEG;
    } else {
      throw new ImageNotValidException("Image not valid. Supported formats: gif, png, jpg, jpeg.");
    }
  }
}
