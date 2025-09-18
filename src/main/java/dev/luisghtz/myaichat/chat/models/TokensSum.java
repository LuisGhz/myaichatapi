package dev.luisghtz.myaichat.chat.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class TokensSum {
  private Long promptTokens;
  private Long completionTokens;
}
