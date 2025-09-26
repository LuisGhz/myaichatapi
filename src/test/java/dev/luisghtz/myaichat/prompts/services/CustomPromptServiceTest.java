package dev.luisghtz.myaichat.prompts.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.UserService;
import dev.luisghtz.myaichat.exceptions.ResourceInUseException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.repositories.CustomRepository;

@ExtendWith(MockitoExtension.class)
class CustomPromptServiceTest {

  @Mock
  CustomRepository promptRepository;

  @Mock
  PromptMessageService promptMessageService;

  @Mock
  UserService userService;

  @InjectMocks
  CustomPromptService customPromptService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(UUID.randomUUID());
    user.setUsername("tester");
    user.setEmail("tester@example.com");
  }

  @Test
  @DisplayName("findAll - should return summary DTO")
  void findAll_shouldReturnSummaries() {
    List<PromptSummaryResDto> sample = List.of();
    when(promptRepository.findAllSummary()).thenReturn(sample);

    PromptsListDtoRes res = customPromptService.findAll();

    assertThat(res).isNotNull();
    assertThat(res.getPrompts()).isSameAs(sample);
    verify(promptRepository).findAllSummary();
  }

  @Test
  @DisplayName("findAllByUserId - should return summary DTO for user")
  void findAllByUserId_shouldReturnSummaries() {
    List<PromptSummaryResDto> sample = List.of();
    UUID id = UUID.randomUUID();
    when(promptRepository.findAllSummaryByUserId(id)).thenReturn(sample);

    PromptsListDtoRes res = customPromptService.findAllByUserId(id);

    assertThat(res).isNotNull();
    assertThat(res.getPrompts()).isSameAs(sample);
    verify(promptRepository).findAllSummaryByUserId(id);
  }

  @Nested
  @DisplayName("create")
  class Create {
    @Test
    @DisplayName("create - should save new prompt when user exists")
    void create_shouldSaveNewPrompt() {
      CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq("name", "content", null);
      when(userService.findById("uid")).thenReturn(Optional.of(user));
      when(promptRepository.save(any(CustomPrompt.class))).thenAnswer(i -> i.getArgument(0));

      CustomPrompt created = customPromptService.create(dto, "uid");

      assertThat(created).isNotNull();
      assertThat(created.getName()).isEqualTo("name");
      assertThat(created.getContent()).isEqualTo("content");
      assertThat(created.getUser()).isEqualTo(user);
      verify(promptMessageService).addPromptMessagesIfExists(dto, created);
      verify(promptRepository).save(created);
    }
  }

  @Nested
  @DisplayName("update")
  class UpdateTests {
    @Test
    @DisplayName("update - should update fields and call message handlers")
    void update_shouldModifyPrompt() {
      UUID uid = UUID.randomUUID();
      CustomPrompt existing = CustomPrompt.builder()
          .id(uid)
          .name("old")
          .content("oldcontent")
          .user(user)
          .createdAt(new Date())
          .build();

      UpdateCustomPromptDtoReq dto = UpdateCustomPromptDtoReq.builder()
          .name("newname")
          .content("newcontent")
          .messages(null)
          .build();

      when(promptRepository.findByIdAndUserId(uid, user.getId())).thenReturn(Optional.of(existing));

      customPromptService.update(uid.toString(), dto, user.getId());

      assertThat(existing.getName()).isEqualTo("newname");
      assertThat(existing.getContent()).isEqualTo("newcontent");
      verify(promptMessageService).addPromptMessagesIfExistsToExistingPrompt(dto, existing);
      verify(promptMessageService).handleUpdatedExistingMessages(dto, existing);
      verify(promptRepository).save(existing);
    }
  }

  @Nested
  @DisplayName("deleteMessage")
  class DeleteMessageTests {
    @Test
    @DisplayName("deleteMessage - should remove message and call message service")
    void deleteMessage_shouldRemoveMessage() throws Exception {
      // Create prompt with a single message
      UUID promptId = UUID.randomUUID();
      CustomPrompt existing = CustomPrompt.builder()
          .id(promptId)
          .name("p")
          .content("c")
          .user(user)
          .messages(Collections.emptyList())
          .build();

      when(promptRepository.findByIdAndUserId(promptId, user.getId())).thenReturn(Optional.of(existing));

      // deleteMessage will try to find message by id; since list is empty it'll throw
      assertThrows(org.springframework.web.server.ResponseStatusException.class,
          () -> customPromptService.deleteMessage(promptId.toString(), UUID.randomUUID().toString(), user.getId()));
    }

    @Test
    @DisplayName("deleteMessage - should delete existing message and persist prompt")
    void deleteMessage_shouldDeleteExistingMessage() throws Exception {
      UUID promptId = UUID.randomUUID();
      UUID messageId = UUID.randomUUID();
      var message = dev.luisghtz.myaichat.prompts.entities.PromptMessage.builder()
          .id(messageId)
          .role("system")
          .content("hello")
          .build();

    CustomPrompt existing = CustomPrompt.builder()
      .id(promptId)
      .name("p")
      .content("c")
      .user(user)
      .messages(new java.util.ArrayList<>(List.of(message)))
      .build();

      // link back the prompt reference (not strictly required but mirrors JPA)
      message.setPrompt(existing);

      when(promptRepository.findByIdAndUserId(promptId, user.getId())).thenReturn(Optional.of(existing));
      when(promptRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

      customPromptService.deleteMessage(promptId.toString(), messageId.toString(), user.getId());

      verify(promptRepository).findByIdAndUserId(promptId, user.getId());
      verify(promptMessageService).deleteByIdAndPromptId(messageId.toString(), promptId.toString());
      verify(promptRepository).save(existing);
      assertThat(existing.getMessages()).isEmpty();
    }
  }

  @Nested
  @DisplayName("delete")
  class DeleteTests {
    @Test
    @DisplayName("delete - should throw when prompt in use")
    void delete_shouldThrowWhenInUse() {
      UUID promptId = UUID.randomUUID();
      CustomPrompt existing = CustomPrompt.builder().id(promptId).user(user).build();
      when(promptRepository.findByIdAndUserId(promptId, user.getId())).thenReturn(Optional.of(existing));
      when(promptRepository.hasAssociatedChats(promptId)).thenReturn(true);

      assertThrows(ResourceInUseException.class, () -> customPromptService.delete(promptId.toString(), user.getId()));
      verify(promptRepository, never()).delete(existing);
    }

    @Test
    @DisplayName("delete - should delete when not in use")
    void delete_shouldRemoveWhenNotInUse() {
      UUID promptId = UUID.randomUUID();
      CustomPrompt existing = CustomPrompt.builder().id(promptId).user(user).build();
      when(promptRepository.findByIdAndUserId(promptId, user.getId())).thenReturn(Optional.of(existing));
      when(promptRepository.hasAssociatedChats(promptId)).thenReturn(false);

      customPromptService.delete(promptId.toString(), user.getId());

      verify(promptRepository).delete(existing);
    }
  }
}
