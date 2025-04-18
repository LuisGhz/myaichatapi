package dev.luisghtz.myaichat.prompts.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.uuid.Generators;

import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.entities.PromptParam;
import dev.luisghtz.myaichat.prompts.repositories.CustomRepository;

@ExtendWith(MockitoExtension.class)
public class CustomPromptServiceTest {

  @Mock
  private CustomRepository promptRepository;
  @Mock
  private PromptParamService promptParamService;
  @Mock
  private PromptMessageService promptMessageService;

  @InjectMocks
  private CustomPromptService customPromptService;

  private CreateCustomPromptDtoReq createCustomPromptDtoReq;
  private CustomPrompt savedCustomPrompt;

  @BeforeEach
  void setUp() {
    // Initialize the request DTO
    createCustomPromptDtoReq = new CreateCustomPromptDtoReq();
    createCustomPromptDtoReq.setName("Test Prompt");
    createCustomPromptDtoReq.setContent("You are a helpful assistant");
    UUID id = Generators.randomBasedGenerator().generate();

    // Initialize the entity that would be returned after saving
    savedCustomPrompt = CustomPrompt.builder()
        .id(id)
        .name("Test Prompt")
        .content("You are a helpful assistant")
        .createdAt(new Date())
        .build();
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
    assertEquals(createCustomPromptDtoReq.getContent(), result.getContent());

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
      assertEquals(createCustomPromptDtoReq.getContent(), entityToSave.getContent());

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

  @Test
  @DisplayName("Should process prompt messages when provided")
  void testCreateWithPromptMessages() {
    // Arrange
    CreateCustomPromptMessagesDto messageDto = new CreateCustomPromptMessagesDto();
    messageDto.setRole("User");
    messageDto.setContent("Hello!");
    createCustomPromptDtoReq.setMessages(Arrays.asList(messageDto));

    // Create the expected saved entity with messages
    PromptMessage promptMessage = PromptMessage.builder()
        .role("User")
        .content("Hello!")
        .prompt(savedCustomPrompt)
        .build();
    savedCustomPrompt.setMessages(List.of(promptMessage));

    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq);

    // Assert
    assertNotNull(result.getMessages());
    assertEquals(1, result.getMessages().size());
    assertEquals("User", result.getMessages().get(0).getRole());
    assertEquals("Hello!", result.getMessages().get(0).getContent());

    // Verify repository was called
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should process prompt params when provided")
  void testCreateWithPromptParams() {
    // Arrange
    CreateCustomPromptParamsDto paramDto = new CreateCustomPromptParamsDto();
    paramDto.setName("temperature");
    paramDto.setValue("0.7");
    createCustomPromptDtoReq.setParams(Arrays.asList(paramDto));

    // Create the expected saved entity with params
    PromptParam promptParam = PromptParam.builder()
        .name("temperature")
        .value("0.7")
        .prompt(savedCustomPrompt)
        .build();
    savedCustomPrompt.setParams(List.of(promptParam));

    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq);

    // Assert
    assertNotNull(result.getParams());
    assertEquals(1, result.getParams().size());
    assertEquals("temperature", result.getParams().get(0).getName());
    assertEquals("0.7", result.getParams().get(0).getValue());

    // Verify repository was called
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should correctly map both params and messages when provided")
  void testCreateWithParamsAndMessages() {
    // Arrange
    // Add message
    CreateCustomPromptMessagesDto messageDto = new CreateCustomPromptMessagesDto();
    messageDto.setRole("Assistant");
    messageDto.setContent("How can I help you?");
    createCustomPromptDtoReq.setMessages(Arrays.asList(messageDto));

    // Add param
    CreateCustomPromptParamsDto paramDto = new CreateCustomPromptParamsDto();
    paramDto.setName("max_tokens");
    paramDto.setValue("1000");
    createCustomPromptDtoReq.setParams(Arrays.asList(paramDto));

    // Create the expected entity with both messages and params
    PromptMessage promptMessage = PromptMessage.builder()
        .role("Assistant")
        .content("How can I help you?")
        .prompt(savedCustomPrompt)
        .build();

    PromptParam promptParam = PromptParam.builder()
        .name("max_tokens")
        .value("1000")
        .prompt(savedCustomPrompt)
        .build();

    savedCustomPrompt.setMessages(List.of(promptMessage));
    savedCustomPrompt.setParams(List.of(promptParam));

    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq);

    // Assert final result
    assertNotNull(result);
    assertNotNull(result.getMessages());
    assertNotNull(result.getParams());
    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getParams().size());
    assertEquals("Assistant", result.getMessages().get(0).getRole());
    assertEquals("How can I help you?", result.getMessages().get(0).getContent());
    assertEquals("max_tokens", result.getParams().get(0).getName());
    assertEquals("1000", result.getParams().get(0).getValue());

    // Verify repository was called
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should handle empty lists for messages and params")
  void testCreateWithEmptyLists() {
    // Arrange - Set empty lists
    createCustomPromptDtoReq.setMessages(List.of());
    createCustomPromptDtoReq.setParams(List.of());

    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq);

    // Assert
    assertNotNull(result);
    assertEquals(savedCustomPrompt.getId(), result.getId());

    // Verify repository was called
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should update name and content when provided in UpdateCustomPromptDtoReq")
  void testUpdateNameAndContent() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    CustomPrompt existingPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Old Name")
        .content("Old Content")
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("New Name");
    updateDto.setContent("New Content");

    when(promptRepository.findById(UUID.fromString(promptId))).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    // Act
    customPromptService.update(promptId, updateDto);

    // Assert
    assertEquals("New Name", existingPrompt.getName());
    assertEquals("New Content", existingPrompt.getContent());
    assertNotNull(existingPrompt.getUpdatedAt());
    verify(promptMessageService).addPromptMessagesIfExistsToExistingPrompt(updateDto, existingPrompt);
    verify(promptParamService).addPromptParamsIfExistsToExistingPrompt(updateDto, existingPrompt);
    verify(promptParamService).handleUpdatedExistingParams(updateDto, existingPrompt);
    verify(promptMessageService).handleUpdatedExistingMessages(updateDto, existingPrompt);
    verify(promptRepository).save(existingPrompt);
  }

  @Test
  @DisplayName("Should update only name if content is null or empty")
  void testUpdateOnlyName() {
    String promptId = UUID.randomUUID().toString();
    CustomPrompt existingPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Old Name")
        .content("Old Content")
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("New Name");
    updateDto.setContent("");

    when(promptRepository.findById(UUID.fromString(promptId))).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    customPromptService.update(promptId, updateDto);

    assertEquals("New Name", existingPrompt.getName());
    assertEquals("Old Content", existingPrompt.getContent());
    verify(promptRepository).save(existingPrompt);
  }

  @Test
  @DisplayName("Should update only content if name is null or empty")
  void testUpdateOnlyContent() {
    String promptId = UUID.randomUUID().toString();
    CustomPrompt existingPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Old Name")
        .content("Old Content")
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("");
    updateDto.setContent("New Content");

    when(promptRepository.findById(UUID.fromString(promptId))).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    customPromptService.update(promptId, updateDto);

    assertEquals("Old Name", existingPrompt.getName());
    assertEquals("New Content", existingPrompt.getContent());
    verify(promptRepository).save(existingPrompt);
  }

  @Test
  @DisplayName("Should throw AppNotFoundException if prompt does not exist")
  void testUpdatePromptNotFound() {
    String promptId = UUID.randomUUID().toString();
    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("Any Name");
    updateDto.setContent("Any Content");

    when(promptRepository.findById(UUID.fromString(promptId))).thenReturn(Optional.empty());

    assertThrows(AppNotFoundException.class, () -> customPromptService.update(promptId, updateDto));
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should call all update helper services with correct arguments")
  void testUpdateCallsHelperServices() {
    String promptId = UUID.randomUUID().toString();
    CustomPrompt existingPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Old Name")
        .content("Old Content")
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("New Name");
    updateDto.setContent("New Content");

    when(promptRepository.findById(UUID.fromString(promptId))).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    customPromptService.update(promptId, updateDto);

    verify(promptMessageService).addPromptMessagesIfExistsToExistingPrompt(updateDto, existingPrompt);
    verify(promptParamService).addPromptParamsIfExistsToExistingPrompt(updateDto, existingPrompt);
    verify(promptParamService).handleUpdatedExistingParams(updateDto, existingPrompt);
    verify(promptMessageService).handleUpdatedExistingMessages(updateDto, existingPrompt);
    verify(promptRepository).save(existingPrompt);
  }

  @Test
  @DisplayName("Should not update if no changes are made")
  void testUpdateNoChanges() {
    String promptId = UUID.randomUUID().toString();
    CustomPrompt existingPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Old Name")
        .content("Old Content")
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("");
    updateDto.setContent("");

    when(promptRepository.findById(UUID.fromString(promptId))).thenReturn(Optional.of(existingPrompt));

    customPromptService.update(promptId, updateDto);

    assertEquals("Old Name", existingPrompt.getName());
    assertEquals("Old Content", existingPrompt.getContent());
  }
}