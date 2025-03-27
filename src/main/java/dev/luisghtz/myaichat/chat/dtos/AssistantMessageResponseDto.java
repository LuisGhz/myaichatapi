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
  private UUID chatId;
  private String content;
  private Integer promptTokens;
  private Integer completionTokens;
  private Integer totalTokens;
}
