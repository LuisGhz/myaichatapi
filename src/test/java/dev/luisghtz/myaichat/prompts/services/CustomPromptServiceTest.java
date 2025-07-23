package dev.luisghtz.myaichat.prompts.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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

import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.UserService;
import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.exceptions.ResourceInUseException;
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
  @Mock
  private UserService userService;

  @InjectMocks
  private CustomPromptService customPromptService;

  private CreateCustomPromptDtoReq createCustomPromptDtoReq;
  private CustomPrompt savedCustomPrompt;
  private User testUser;
  private String userId;
  private UUID userIdUuid;

  @BeforeEach
  void setUp() {
    // Initialize test user data
    userIdUuid = UUID.randomUUID();
    userId = userIdUuid.toString();
    testUser = new User();
    testUser.setId(userIdUuid);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");

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
        .user(testUser)
        .createdAt(new Date())
        .build();
  }

  @Test
  @DisplayName("Should successfully create a custom prompt")
  void testCreateCustomPrompt() throws Exception {
    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);
    
    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq, userId);

    // Assert
    assertNotNull(result);
    assertEquals(savedCustomPrompt.getId(), result.getId());
    assertEquals(createCustomPromptDtoReq.getName(), result.getName());
    assertEquals(createCustomPromptDtoReq.getContent(), result.getContent());

    // Verify repository was called with the correct entity
    verify(userService).findById(userId);
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should map DTO fields correctly to entity")
  void testDtoToEntityMapping() throws Exception {
    // Arrange
    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    // Override the default mock behavior to capture the actual entity being saved
    when(promptRepository.save(any(CustomPrompt.class))).thenAnswer(invocation -> {
      CustomPrompt entityToSave = invocation.getArgument(0);

      // Assert the mapping is correct
      assertEquals(createCustomPromptDtoReq.getName(), entityToSave.getName());
      assertEquals(createCustomPromptDtoReq.getContent(), entityToSave.getContent());

      return savedCustomPrompt;
    });

    // Act
    customPromptService.create(createCustomPromptDtoReq, userId);

    // Verify repository was called
    verify(userService).findById(userId);
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should handle repository exceptions")
  void testRepositoryException() {
    // Arrange
    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    when(promptRepository.save(any(CustomPrompt.class))).thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> customPromptService.create(createCustomPromptDtoReq, userId));
    verify(userService).findById(userId);
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should handle user not found exception")
  void testUserNotFoundException() {
    // Arrange
    when(userService.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(RuntimeException.class, () -> customPromptService.create(createCustomPromptDtoReq, userId));
    verify(userService).findById(userId);
    verify(promptRepository, never()).save(any(CustomPrompt.class));
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

    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq, userId);

    // Assert
    assertNotNull(result.getMessages());
    assertEquals(1, result.getMessages().size());
    assertEquals("User", result.getMessages().get(0).getRole());
    assertEquals("Hello!", result.getMessages().get(0).getContent());

    // Verify repository was called
    verify(userService).findById(userId);
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

    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq, userId);

    // Assert
    assertNotNull(result.getParams());
    assertEquals(1, result.getParams().size());
    assertEquals("temperature", result.getParams().get(0).getName());
    assertEquals("0.7", result.getParams().get(0).getValue());

    // Verify repository was called
    verify(userService).findById(userId);
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

    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq, userId);

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
    verify(userService).findById(userId);
    verify(promptRepository).save(any(CustomPrompt.class));
  }

  @Test
  @DisplayName("Should handle empty lists for messages and params")
  void testCreateWithEmptyLists() {
    // Arrange - Set empty lists
    createCustomPromptDtoReq.setMessages(List.of());
    createCustomPromptDtoReq.setParams(List.of());

    when(userService.findById(userId)).thenReturn(Optional.of(testUser));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(savedCustomPrompt);

    // Act
    CustomPrompt result = customPromptService.create(createCustomPromptDtoReq, userId);

    // Assert
    assertNotNull(result);
    assertEquals(savedCustomPrompt.getId(), result.getId());

    // Verify repository was called
    verify(userService).findById(userId);
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
        .user(testUser)
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("New Name");
    updateDto.setContent("New Content");

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    // Act
    customPromptService.update(promptId, updateDto, userIdUuid);

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
        .user(testUser)
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("New Name");
    updateDto.setContent("");

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    customPromptService.update(promptId, updateDto, userIdUuid);

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
        .user(testUser)
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("");
    updateDto.setContent("New Content");

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    customPromptService.update(promptId, updateDto, userIdUuid);

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

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.empty());

    assertThrows(AppNotFoundException.class, () -> customPromptService.update(promptId, updateDto, userIdUuid));
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
        .user(testUser)
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("New Name");
    updateDto.setContent("New Content");

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(existingPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(existingPrompt);

    customPromptService.update(promptId, updateDto, userIdUuid);

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
        .user(testUser)
        .createdAt(new Date())
        .build();

    UpdateCustomPromptDtoReq updateDto = new UpdateCustomPromptDtoReq();
    updateDto.setName("");
    updateDto.setContent("");

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(existingPrompt));

    customPromptService.update(promptId, updateDto, userIdUuid);

    assertEquals("Old Name", existingPrompt.getName());
    assertEquals("Old Content", existingPrompt.getContent());
  }

  @Test
  @DisplayName("Should delete param successfully when prompt and param exist")
  void testDeleteParamSuccess() throws Exception {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String paramId = UUID.randomUUID().toString();

    PromptParam param = PromptParam.builder()
        .id(UUID.fromString(paramId))
        .name("temperature")
        .value("0.7")
        .build();

    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .user(testUser)
        .params(new ArrayList<>(List.of(param)))
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(customPrompt);

    // Act
    customPromptService.deleteParam(promptId, paramId, userIdUuid);

    // Assert
    assertTrue(customPrompt.getParams().isEmpty());
    verify(promptParamService).deleteByIdAndPromptId(paramId, promptId);
    verify(promptRepository).save(customPrompt);
  }

  @Test
  @DisplayName("Should throw AppNotFoundException if prompt does not exist in deleteParam")
  void testDeleteParamPromptNotFound() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String paramId = UUID.randomUUID().toString();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AppNotFoundException.class, () -> customPromptService.deleteParam(promptId, paramId, userIdUuid));
    verify(promptParamService, never()).deleteByIdAndPromptId(anyString(), anyString());
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw AppNotFoundException if param does not exist in deleteParam")
  void testDeleteParamParamNotFound() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String paramId = UUID.randomUUID().toString();

    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .user(testUser)
        .params(new java.util.ArrayList<>()) // No params
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));

    // Act & Assert
    assertThrows(AppNotFoundException.class, () -> customPromptService.deleteParam(promptId, paramId, userIdUuid));
    verify(promptParamService, never()).deleteByIdAndPromptId(anyString(), anyString());
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should propagate exception thrown by promptParamService.deleteByIdAndPromptId")
  void testDeleteParamServiceThrows() throws Exception {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String paramId = UUID.randomUUID().toString();

    PromptParam param = PromptParam.builder()
        .id(UUID.fromString(paramId))
        .name("temperature")
        .value("0.7")
        .build();

    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .user(testUser)
        .params(new java.util.ArrayList<>(List.of(param)))
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    doThrow(new RuntimeException("Delete failed")).when(promptParamService).deleteByIdAndPromptId(paramId, promptId);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> customPromptService.deleteParam(promptId, paramId, userIdUuid));
    // The param should have been removed from the prompt's params list before the exception
    assertTrue(customPrompt.getParams().isEmpty());
    verify(promptParamService).deleteByIdAndPromptId(paramId, promptId);
    // promptRepository.save should not be called if exception is thrown before it
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete message successfully when prompt and message exist")
  void testDeleteMessageSuccess() throws Exception {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String messageId = UUID.randomUUID().toString();

    PromptMessage message = PromptMessage.builder()
        .id(UUID.fromString(messageId))
        .role("User")
        .content("Hello!")
        .build();

    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .user(testUser)
        .messages(new ArrayList<>(List.of(message)))
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    when(promptRepository.save(any(CustomPrompt.class))).thenReturn(customPrompt);

    // Act
    customPromptService.deleteMessage(promptId, messageId, userIdUuid);

    // Assert
    assertTrue(customPrompt.getMessages().isEmpty());
    verify(promptMessageService).deleteByIdAndPromptId(messageId, promptId);
    verify(promptRepository).save(customPrompt);
  }

  @Test
  @DisplayName("Should throw AppNotFoundException if prompt does not exist in deleteMessage")
  void testDeleteMessagePromptNotFound() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String messageId = UUID.randomUUID().toString();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AppNotFoundException.class, () -> customPromptService.deleteMessage(promptId, messageId, userIdUuid));
    verify(promptMessageService, never()).deleteByIdAndPromptId(anyString(), anyString());
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw AppNotFoundException if message does not exist in deleteMessage")
  void testDeleteMessageMessageNotFound() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String messageId = UUID.randomUUID().toString();

    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .user(testUser)
        .messages(new ArrayList<>()) // No messages
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));

    // Act & Assert
    assertThrows(AppNotFoundException.class, () -> customPromptService.deleteMessage(promptId, messageId, userIdUuid));
    verify(promptMessageService, never()).deleteByIdAndPromptId(anyString(), anyString());
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should propagate exception thrown by promptMessageService.deleteByIdAndPromptId")
  void testDeleteMessageServiceThrows() throws Exception {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    String messageId = UUID.randomUUID().toString();

    PromptMessage message = PromptMessage.builder()
        .id(UUID.fromString(messageId))
        .role("User")
        .content("Hello!")
        .build();

    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .user(testUser)
        .messages(new ArrayList<>(List.of(message)))
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    doThrow(new RuntimeException("Delete failed")).when(promptMessageService).deleteByIdAndPromptId(messageId, promptId);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> customPromptService.deleteMessage(promptId, messageId, userIdUuid));
    // The message should have been removed from the prompt's messages list before the exception
    assertTrue(customPrompt.getMessages().isEmpty());
    verify(promptMessageService).deleteByIdAndPromptId(messageId, promptId);
    // promptRepository.save should not be called if exception is thrown before it
    verify(promptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete prompt successfully when prompt exists")
  void testDeletePromptSuccess() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Prompt to delete")
        .content("Some content")
        .user(testUser)
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    when(promptRepository.hasAssociatedChats(UUID.fromString(promptId))).thenReturn(false);

    // Act
    customPromptService.delete(promptId, userIdUuid);

    // Assert
    verify(promptRepository).findByIdAndUserId(UUID.fromString(promptId), userIdUuid);
    verify(promptRepository).hasAssociatedChats(UUID.fromString(promptId));
    verify(promptRepository).delete(customPrompt);
  }

  @Test
  @DisplayName("Should throw AppNotFoundException if prompt does not exist in delete")
  void testDeletePromptNotFound() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AppNotFoundException.class, () -> customPromptService.delete(promptId, userIdUuid));
    verify(promptRepository).findByIdAndUserId(UUID.fromString(promptId), userIdUuid);
    verify(promptRepository, never()).delete(any());
    verify(promptRepository, never()).hasAssociatedChats(any());
  }

  @Test
  @DisplayName("Should throw ResourceInUseException if prompt has associated chats")
  void testDeletePromptInUse() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Prompt to delete")
        .content("Some content")
        .user(testUser)
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    when(promptRepository.hasAssociatedChats(UUID.fromString(promptId))).thenReturn(true);

    // Act & Assert
    assertThrows(ResourceInUseException.class, () -> customPromptService.delete(promptId, userIdUuid));
    verify(promptRepository).findByIdAndUserId(UUID.fromString(promptId), userIdUuid);
    verify(promptRepository).hasAssociatedChats(UUID.fromString(promptId));
    verify(promptRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should propagate exception thrown by repository.delete in delete")
  void testDeletePromptRepositoryThrows() {
    // Arrange
    String promptId = UUID.randomUUID().toString();
    CustomPrompt customPrompt = CustomPrompt.builder()
        .id(UUID.fromString(promptId))
        .name("Prompt to delete")
        .content("Some content")
        .user(testUser)
        .build();

    when(promptRepository.findByIdAndUserId(UUID.fromString(promptId), userIdUuid)).thenReturn(Optional.of(customPrompt));
    when(promptRepository.hasAssociatedChats(UUID.fromString(promptId))).thenReturn(false);
    doThrow(new RuntimeException("Delete failed")).when(promptRepository).delete(customPrompt);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> customPromptService.delete(promptId, userIdUuid));
    verify(promptRepository).findByIdAndUserId(UUID.fromString(promptId), userIdUuid);
    verify(promptRepository).hasAssociatedChats(UUID.fromString(promptId));
    verify(promptRepository).delete(customPrompt);
  }

}