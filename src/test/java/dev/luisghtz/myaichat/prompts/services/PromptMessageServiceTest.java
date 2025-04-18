package dev.luisghtz.myaichat.prompts.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.repositories.PromptMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromptMessageServiceTest {

  @Mock
  private PromptMessageRepository promptMessageRepository;

  @InjectMocks
  private PromptMessageService promptMessageService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    promptMessageService = new PromptMessageService(promptMessageRepository);
  }

  @Test
  @DisplayName("deleteByIdAndPromptId should call repository with correct UUIDs")
  void testDeleteByIdAndPromptId() {
    String id = UUID.randomUUID().toString();
    String promptId = UUID.randomUUID().toString();

    promptMessageService.deleteByIdAndPromptId(id, promptId);

    verify(promptMessageRepository).deleteByIdAndPromptId(UUID.fromString(id), UUID.fromString(promptId));
  }

  @Test
  @DisplayName("addPromptMessagesIfExists should not set messages if input is null or empty")
  void testAddPromptMessagesIfExistsNullOrEmpty() {
    CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    // Null messages
    dto.setMessages(null);
    promptMessageService.addPromptMessagesIfExists(dto, prompt);
    assertNull(prompt.getMessages());

    // Empty messages
    dto.setMessages(Collections.emptyList());
    promptMessageService.addPromptMessagesIfExists(dto, prompt);
    assertNull(prompt.getMessages());
  }

  @Test
  @DisplayName("addPromptMessagesIfExists should map and set messages")
  void testAddPromptMessagesIfExistsWithMessages() {
    CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    CreateCustomPromptMessagesDto msgDto = new CreateCustomPromptMessagesDto();
    msgDto.setRole("user");
    msgDto.setContent("hello");
    dto.setMessages(List.of(msgDto));

    promptMessageService.addPromptMessagesIfExists(dto, prompt);

    assertNotNull(prompt.getMessages());
    assertEquals(1, prompt.getMessages().size());
    PromptMessage pm = prompt.getMessages().get(0);
    assertEquals("user", pm.getRole());
    assertEquals("hello", pm.getContent());
    assertEquals(prompt, pm.getPrompt());
  }

  @Test
  @DisplayName("addPromptMessagesIfExists should throw on invalid role/content")
  void testAddPromptMessagesIfExistsInvalidRoleOrContent() {
    CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    CreateCustomPromptMessagesDto msgDto = new CreateCustomPromptMessagesDto();
    msgDto.setRole(""); // Invalid role
    msgDto.setContent("hello");
    dto.setMessages(List.of(msgDto));

    assertThrows(AppMethodArgumentNotValidException.class,
        () -> promptMessageService.addPromptMessagesIfExists(dto, prompt));

    msgDto.setRole("user");
    msgDto.setContent(""); // Invalid content
    assertThrows(AppMethodArgumentNotValidException.class,
        () -> promptMessageService.addPromptMessagesIfExists(dto, prompt));
  }

  @Test
  @DisplayName("addPromptMessagesIfExistsToExistingPrompt should not add if messages null/empty")
  void testAddPromptMessagesIfExistsToExistingPromptNullOrEmpty() {
    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    dto.setMessages(null);
    promptMessageService.addPromptMessagesIfExistsToExistingPrompt(dto, prompt);
    assertNull(prompt.getMessages());

    dto.setMessages(Collections.emptyList());
    promptMessageService.addPromptMessagesIfExistsToExistingPrompt(dto, prompt);
    assertNull(prompt.getMessages());
  }

  @Test
  @DisplayName("addPromptMessagesIfExistsToExistingPrompt should add new messages")
  void testAddPromptMessagesIfExistsToExistingPromptAddsNew() {
    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().messages(new ArrayList<>()).build();

    UpdateCustomPromptMessagesDto msgDto = new UpdateCustomPromptMessagesDto();
    msgDto.setId(null);
    msgDto.setRole("assistant");
    msgDto.setContent("hi");
    dto.setMessages(List.of(msgDto));

    promptMessageService.addPromptMessagesIfExistsToExistingPrompt(dto, prompt);

    assertNotNull(prompt.getMessages());
    assertEquals(1, prompt.getMessages().size());
    PromptMessage pm = prompt.getMessages().get(0);
    assertEquals("assistant", pm.getRole());
    assertEquals("hi", pm.getContent());
    assertEquals(prompt, pm.getPrompt());
  }

  @Test
  @DisplayName("addPromptMessagesIfExistsToExistingPrompt should throw if role/content invalid")
  void testAddPromptMessagesIfExistsToExistingPromptThrowsOnInvalid() {
    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().messages(new ArrayList<>()).build();

    UpdateCustomPromptMessagesDto msgDto = new UpdateCustomPromptMessagesDto();
    msgDto.setId(null);
    msgDto.setRole(""); // Invalid
    msgDto.setContent("hi");
    dto.setMessages(List.of(msgDto));

    assertThrows(AppMethodArgumentNotValidException.class,
        () -> promptMessageService.addPromptMessagesIfExistsToExistingPrompt(dto, prompt));

    msgDto.setRole("assistant");
    msgDto.setContent(""); // Invalid
    assertThrows(AppMethodArgumentNotValidException.class,
        () -> promptMessageService.addPromptMessagesIfExistsToExistingPrompt(dto, prompt));
  }

  @Test
  @DisplayName("handleUpdatedExistingMessages should update existing messages")
  void testHandleUpdatedExistingMessagesUpdates() {
    UUID msgId = UUID.randomUUID();
    PromptMessage existing = PromptMessage.builder()
        .id(msgId)
        .role("user")
        .content("old")
        .build();
    CustomPrompt prompt = CustomPrompt.builder()
        .messages(new ArrayList<>(List.of(existing)))
        .build();

    UpdateCustomPromptMessagesDto updateDto = new UpdateCustomPromptMessagesDto();
    updateDto.setId(msgId);
    updateDto.setRole("assistant");
    updateDto.setContent("new");

    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    dto.setMessages(List.of(updateDto));

    promptMessageService.handleUpdatedExistingMessages(dto, prompt);

    assertEquals("assistant", existing.getRole());
    assertEquals("new", existing.getContent());
  }

  @Test
  @DisplayName("handleUpdatedExistingMessages should only update non-null fields")
  void testHandleUpdatedExistingMessagesPartialUpdate() {
    UUID msgId = UUID.randomUUID();
    PromptMessage existing = PromptMessage.builder()
        .id(msgId)
        .role("user")
        .content("old")
        .build();
    CustomPrompt prompt = CustomPrompt.builder()
        .messages(new ArrayList<>(List.of(existing)))
        .build();

    UpdateCustomPromptMessagesDto updateDto = new UpdateCustomPromptMessagesDto();
    updateDto.setId(msgId);
    updateDto.setRole(null);
    updateDto.setContent("new content");

    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    dto.setMessages(List.of(updateDto));

    promptMessageService.handleUpdatedExistingMessages(dto, prompt);

    assertEquals("user", existing.getRole());
    assertEquals("new content", existing.getContent());
  }

}