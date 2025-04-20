package dev.luisghtz.myaichat.chat.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensSum {
  private Long promptTokens;
  private Long completionTokens;
}
