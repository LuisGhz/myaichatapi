package dev.luisghtz.myaichat.ai.models;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class AppModelsTest {

  @Test
  void testAllStringValuesContainsAllModels() {
    List<String> models = AppModels.allStringValues();
    assertTrue(models.contains(AppModels.GPT_4O_MINI.getKey()));
    assertTrue(models.contains(AppModels.GPT_4O.getKey()));
    assertTrue(models.contains(AppModels.GPT_4_1.getKey()));
    assertTrue(models.contains(AppModels.GPT_4_1_MINI.getKey()));
    assertTrue(models.contains(AppModels.O4_MINI.getKey()));
    assertTrue(models.contains(AppModels.GEMINI_FLASH_2_0_LITE.getKey()));
    assertTrue(models.contains(AppModels.GEMINI_FLASH_2_0.getKey()));
    assertTrue(models.contains(AppModels.GEMINI_FLASH_2_5_FLASH.getKey()));
    assertTrue(models.contains(AppModels.GEMINI_FLASH_2_5_PRO.getKey()));
    assertEquals(9, models.size());
  }

  @Test
  void testGetMaxTokensReturnsCorrectValues() {
    assertEquals(5_000, AppModels.getMaxTokens(AppModels.GPT_4O_MINI.getKey()));
    assertEquals(10_000, AppModels.getMaxTokens(AppModels.GPT_4O.getKey()));
    assertEquals(10_000, AppModels.getMaxTokens(AppModels.GPT_4_1.getKey()));
    assertEquals(10_000, AppModels.getMaxTokens(AppModels.GPT_4_1_MINI.getKey()));
    assertEquals(10_000, AppModels.getMaxTokens(AppModels.O4_MINI.getKey()));
    assertEquals(5_000, AppModels.getMaxTokens(AppModels.GEMINI_FLASH_2_0_LITE.getKey()));
    assertEquals(8_000, AppModels.getMaxTokens(AppModels.GEMINI_FLASH_2_0.getKey()));
    assertEquals(10_000, AppModels.getMaxTokens(AppModels.GEMINI_FLASH_2_5_FLASH.getKey()));
    assertEquals(10_000, AppModels.getMaxTokens(AppModels.GEMINI_FLASH_2_5_PRO.getKey()));
  }

  @Test
  void testGetMaxTokensThrowsOnInvalidModel() {
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      AppModels.getMaxTokens("invalid-model");
    });
    assertTrue(exception.getMessage().contains("Invalid model"));
  }
}
