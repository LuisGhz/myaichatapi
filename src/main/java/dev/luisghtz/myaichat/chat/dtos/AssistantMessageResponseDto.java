package dev.luisghtz.myaichat.chat.dtos;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantMessageResponseDto {
  private String content;
  private Boolean isLastChunk;
  
  // Properties only present in the last chunk
  private UUID chatId;
  private String chatTitle;
  private Integer promptTokens;
  private Integer completionTokens;
  private Integer totalTokens;
  private Long totalChatPromptTokens;
  private Long totalChatCompletionTokens;
}
