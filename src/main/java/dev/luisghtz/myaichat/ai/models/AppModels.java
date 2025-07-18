package dev.luisghtz.myaichat.ai.models;

import java.util.List;
import java.util.Map;

public class AppModels {
  public static final AppModel GPT_4O_MINI = new AppModel("gpt-4o-mini", 5_000);
  public static final AppModel GPT_4O = new AppModel("gpt-4o", 10_000);
  public static final AppModel GEMINI_FLASH_2_0_LITE = new AppModel("gemini-2.0-flash-lite", 5_000);
  public static final AppModel GEMINI_FLASH_2_0 = new AppModel("gemini-2.0-flash", 8_000);
  public static final AppModel GEMINI_FLASH_2_5_FLASH = new AppModel("gemini-2.5-flash", 10_000);
  public static final AppModel GEMINI_FLASH_2_5_PRO = new AppModel("gemini-2.5-pro", 10_000);

  private static final Map<String, Integer> MODEL_MAX_TOKENS = Map.of(
      GPT_4O_MINI.getKey(), GPT_4O_MINI.getMaxTokens(),
      GPT_4O.getKey(), GPT_4O.getMaxTokens(),
      GEMINI_FLASH_2_0_LITE.getKey(), GEMINI_FLASH_2_0_LITE.getMaxTokens(),
      GEMINI_FLASH_2_0.getKey(), GEMINI_FLASH_2_0.getMaxTokens(),
      GEMINI_FLASH_2_5_FLASH.getKey(), GEMINI_FLASH_2_5_FLASH.getMaxTokens(),
      GEMINI_FLASH_2_5_PRO.getKey(), GEMINI_FLASH_2_5_PRO.getMaxTokens());

  private AppModels() {
  }

  public static final List<String> allStringValues() {
    return List.of(
        GPT_4O_MINI.getKey(),
        GPT_4O.getKey(),
        GEMINI_FLASH_2_0_LITE.getKey(),
        GEMINI_FLASH_2_0.getKey(),
        GEMINI_FLASH_2_5_FLASH.getKey(),
        GEMINI_FLASH_2_5_PRO.getKey());
  }

  public static int getMaxTokens(String model) {
    Integer maxTokens = MODEL_MAX_TOKENS.get(model);
    if (maxTokens == null)
      throw new IllegalArgumentException("Invalid model: " + model);
    return maxTokens;
  }

}
