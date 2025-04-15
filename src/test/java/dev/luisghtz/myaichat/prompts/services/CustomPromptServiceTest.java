package dev.luisghtz.myaichat.prompts.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.uuid.Generators;

import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.repositories.CustomRepository;





@ExtendWith(MockitoExtension.class)
public class CustomPromptServiceTest {

  @Mock
  private CustomRepository promptRepository;

  @InjectMocks
  private CustomPromptService customPromptService;

  private CreateCustomPromptDtoReq createCustomPromptDtoReq;
  private CustomPrompt savedCustomPrompt;

  @BeforeEach
  void setUp() {
    // Initialize the request DTO
    createCustomPromptDtoReq = new CreateCustomPromptDtoReq();
    createCustomPromptDtoReq.setName("Test Prompt");
    createCustomPromptDtoReq.setModel("gpt-4");
    createCustomPromptDtoReq.setSystemMessage("You are a helpful assistant");
    UUID id = Generators.randomBasedGenerator().generate();
    // Initialize the entity that would be returned after saving
    savedCustomPrompt = CustomPrompt.builder()
        .id(id)
        .name("Test Prompt")
        .model("gpt-4")
        .systemMessage("You are a helpful assistant")
        .build();

    // Configure default mock behavior
  }
  
  @Test
  @DisplayName("Should successfully create a custom prompt")
  void testCreateCustomPrompt() throws Exception {
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);
    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq);

    // Assert
    assertNotNull(result);
    assertEquals(savedCustomPrompt.getId(), result.getId());
    assertEquals(createCustomPromptDtoReq.getName(), result.getName());
    assertEquals(createCustomPromptDtoReq.getModel(), result.getModel());
    assertEquals(createCustomPromptDtoReq.getSystemMessage(), result.getSystemMessage());

    // Verify repository was called with the correct entity
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should map DTO fields correctly to entity")
  void testDtoToEntityMapping() throws Exception {
    // Arrange
    // Override the default mock behavior to capture the actual entity being saved
    when(promptRepository.save(any(CustomPrompt.class))).thenAnswer(invocation -> {
      CustomPrompt entityToSave = invocation.getArgument(0);
      
      // Assert the mapping is correct
      assertEquals(createCustomPromptDtoReq.getName(), entityToSave.getName());
      assertEquals(createCustomPromptDtoReq.getModel(), entityToSave.getModel());
      assertEquals(createCustomPromptDtoReq.getSystemMessage(), entityToSave.getSystemMessage());
      
      return savedCustomPrompt;
    });

    // Act
    customPromptService.create(createCustomPromptDtoReq);
    
    // Verify repository was called
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should handle repository exceptions")
  void testRepositoryException() {
    // Arrange
    when(promptRepository.save(any(CustomPrompt.class))).thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> customPromptService.create(createCustomPromptDtoReq));
    verify(promptRepository).save(any(CustomPrompt.class));
  }
}