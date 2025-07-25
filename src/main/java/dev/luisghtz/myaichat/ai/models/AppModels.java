package dev.luisghtz.myaichat.ai.models;

import java.util.List;
import java.util.Map;

public class AppModels {
  public static final AppModel GPT_4O_MINI = new AppModel("gpt-4o-mini", 5_000, 0.5);
  public static final AppModel GPT_4O = new AppModel("gpt-4o", 10_000, 1);
  public static final AppModel GPT_4_1 = new AppModel("gpt-4.1-2025-04-14", 10_000, 0.5);
  public static final AppModel GPT_4_1_MINI = new AppModel("gpt-4.1-mini-2025-04-14", 10_000, 0.5);
  public static final AppModel O4_MINI = new AppModel("o4-mini", 10_000, 1);
  public static final AppModel GEMINI_FLASH_2_0_LITE = new AppModel("gemini-2.0-flash-lite", 5_000, 1);
  public static final AppModel GEMINI_FLASH_2_0 = new AppModel("gemini-2.0-flash", 8_000, 1);
  public static final AppModel GEMINI_FLASH_2_5_FLASH = new AppModel("gemini-2.5-flash", 10_000, 1);
  public static final AppModel GEMINI_FLASH_2_5_PRO = new AppModel("gemini-2.5-pro", 10_000, 1);

  private static final Map<String, Integer> MODEL_MAX_TOKENS = Map.of(
      GPT_4O_MINI.getKey(), GPT_4O_MINI.getMaxTokens(),
      GPT_4O.getKey(), GPT_4O.getMaxTokens(),
      GPT_4_1.getKey(), GPT_4_1.getMaxTokens(),
      GPT_4_1_MINI.getKey(), GPT_4_1_MINI.getMaxTokens(),
      O4_MINI.getKey(), O4_MINI.getMaxTokens(),
      GEMINI_FLASH_2_0_LITE.getKey(), GEMINI_FLASH_2_0_LITE.getMaxTokens(),
      GEMINI_FLASH_2_0.getKey(), GEMINI_FLASH_2_0.getMaxTokens(),
      GEMINI_FLASH_2_5_FLASH.getKey(), GEMINI_FLASH_2_5_FLASH.getMaxTokens(),
      GEMINI_FLASH_2_5_PRO.getKey(), GEMINI_FLASH_2_5_PRO.getMaxTokens());

  private static final Map<String, Double> MODEL_TEMPERATURES = Map.of(
      GPT_4O_MINI.getKey(), GPT_4O_MINI.getTemperature(),
      GPT_4O.getKey(), GPT_4O.getTemperature(),
      GPT_4_1.getKey(), GPT_4_1.getTemperature(),
      GPT_4_1_MINI.getKey(), GPT_4_1_MINI.getTemperature(),
      O4_MINI.getKey(), O4_MINI.getTemperature(),
      GEMINI_FLASH_2_0_LITE.getKey(), GEMINI_FLASH_2_0_LITE.getTemperature(),
      GEMINI_FLASH_2_0.getKey(), GEMINI_FLASH_2_0.getTemperature(),
      GEMINI_FLASH_2_5_FLASH.getKey(), GEMINI_FLASH_2_5_FLASH.getTemperature(),
      GEMINI_FLASH_2_5_PRO.getKey(), GEMINI_FLASH_2_5_PRO.getTemperature());

  private AppModels() {
  }

  public static final List<String> allStringValues() {
    return List.of(
        GPT_4O_MINI.getKey(),
        GPT_4O.getKey(),
        GPT_4_1.getKey(),
        GPT_4_1_MINI.getKey(),
        O4_MINI.getKey(),
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

  public static double getTemperature(String model) {
    double temperature = MODEL_TEMPERATURES.getOrDefault(model, 0.0);
    return temperature;
  }

}
