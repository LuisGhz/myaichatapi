package dev.luisghtz.myaichat.chat.utils;

import dev.luisghtz.myaichat.chat.entities.AppMessage;
import dev.luisghtz.myaichat.chat.entities.Chat;
import dev.luisghtz.myaichat.chat.dtos.NewMessageRequestDto;

import java.util.Date;

import org.springframework.ai.chat.model.ChatResponse;

public class MessagesUtils {
  public static AppMessage processUserMessage(NewMessageRequestDto newMessageRequestDto, Chat chat, String fileUrl) {
    var newMessage = AppMessage.builder()
        .role("User")
  .content(newMessageRequestDto.getContent())
        .createdAt(new Date())
        .chat(chat)
        .build();
    newMessage = addFileUrlIfApply(newMessage, fileUrl);
    return newMessage;
  }

  public static AppMessage createAssistantMessage(ChatResponse chatResponse, Chat chat) {
    var usage = chatResponse.getMetadata().getUsage();
    return AppMessage.builder()
        .role("Assistant")
        .content(chatResponse.getResult().getOutput().getText())
        .createdAt(new Date())
        .promptTokens(usage.getPromptTokens())
        .completionTokens(usage.getCompletionTokens())
        .totalTokens(usage.getTotalTokens())
        .chat(chat)
        .build();
  }

  private static AppMessage addFileUrlIfApply(AppMessage message, String fileUrl) {
    if (fileUrl != null && !fileUrl.isEmpty())
      message.setFileUrl(fileUrl);
    return message;
  }

}
