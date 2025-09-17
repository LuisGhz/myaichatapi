package dev.luisghtz.myaichat.ai.services;

import java.util.List;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import dev.luisghtz.myaichat.ai.models.AIProviderService;
import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.ai.utils.ChatClientToolsUtil;
import dev.luisghtz.myaichat.ai.utils.MessagesUtil;
import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.exceptions.FileNotValidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Service("openAIService")
@RequiredArgsConstructor
@Log4j2
public class OpenAIService implements AIProviderService {
  private final ChatClient openAIChatClient;
  private final String TITLE_PROMPT = "Generate a concise title of no more than 5 words that summarizes this conversation, "
      + "avoid to use markdown styles, title should be only text. "
      + "The title should be in the same language as the conversation.";
  private final ChatClientToolsUtil chatClientUtil;

  public Flux<ChatResponse> getAssistantMessage(List<AppMessage> messages, Chat chat) {
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
    var temperature = AppModels.getTemperature(chat.getModel());

    OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model(chat.getModel())
        .maxCompletionTokens(chat.getMaxOutputTokens().intValue())
        .temperature(temperature)
        .build();
    var chatRequest = chatClientUtil.getChatClientRequestSpec(openAIChatClient, chat);
    var chatResponse = chatRequest.messages(modelMessages).options(options).stream().chatResponse();

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
    if (message.getFileUrl() != null &&
        message.getId() == null) {
      try {
        MimeType mimeType = getMimeType(message.getFileUrl());
        return UserMessage.builder()
            .text(message.getContent())
            .media(new Media(mimeType, new URI(message.getFileUrl())))
            .build();
      } catch (FileNotValidException e) {
        // Rethrow the FileNotValidException to maintain proper error handling
        throw e;
      } catch (Exception e) {
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
