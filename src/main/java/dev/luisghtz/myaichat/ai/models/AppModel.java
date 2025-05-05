package dev.luisghtz.myaichat.ai.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AppModel {
  private String key;
  private int maxTokens;
}
