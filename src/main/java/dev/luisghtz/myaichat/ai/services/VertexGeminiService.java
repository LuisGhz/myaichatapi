package dev.luisghtz.myaichat.ai.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.Media;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import dev.luisghtz.myaichat.ai.models.AIProviderService;
import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.ImageNotValidException;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
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
    addSystemMessage(chat, modelMessages);
    addInitialMessagesIfApply(chat, modelMessages);

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
        .maxOutputTokens(AppModels.getMaxTokens(chat.getModel()))
        .build();
    ChatResponse chatResponse = vertextAIChatClient.prompt()
        .messages(modelMessages).options(options).call().chatResponse();

    return chatResponse;
  }

  @Override
  public String generateTitle(Chat chat, String userMessage, String assistantMessage) {
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

    String titleResponse = vertextAIChatClient.prompt()
        .messages(titleMessages).options(titleOptions).call().content();
    log.debug("Title response: {}", titleResponse);
    return titleResponse;
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

  private void addInitialMessagesIfApply(Chat chat, List<Message> messages) {
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

  private void addSystemMessage(Chat chat, List<Message> messages) {
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
}
