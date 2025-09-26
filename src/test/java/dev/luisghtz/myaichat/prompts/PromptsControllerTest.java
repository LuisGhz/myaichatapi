package dev.luisghtz.myaichat.prompts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.luisghtz.myaichat.configurationMock.AIModelsControllerTestConfiguration;
import dev.luisghtz.myaichat.configurationMock.jwt.JwtPermitAllTestConfiguration;
import dev.luisghtz.myaichat.configurationMock.UserJwtDataTestConfiguration;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;

@WebMvcTest(PromptsController.class)
@Import({
    AIModelsControllerTestConfiguration.class,
    JwtPermitAllTestConfiguration.class,
    UserJwtDataTestConfiguration.class,
})
@ActiveProfiles("test")
public class PromptsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CustomPromptService customPromptService;

  @Autowired
  private ObjectMapper objectMapper;

  private CreateCustomPromptDtoReq createRequest;
  private CustomPrompt customPrompt;
  private UUID promptId;
  private UUID existingParamId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    promptId = UUID.randomUUID();
    existingParamId = UUID.randomUUID();
    userId = UUID.randomUUID();

    // Setup request DTO
    createRequest = new CreateCustomPromptDtoReq();
    createRequest.setName("Test Prompt");
    createRequest.setContent("You are a helpful assistant");

    customPrompt = CustomPrompt.builder()
        .id(promptId)
        .name("Test Prompt")
        .content("You are a helpful assistant")
        .build();
  }

  @Nested
  @DisplayName("GET Endpoints")
  class GetEndpoints {

    @Test
    @DisplayName("GET /api/custom-prompts/all - Should return all prompts")
    void testGetAllPrompts() throws Exception {
      // Arrange
      PromptSummaryResDto prompt1 = new PromptSummaryResDto();
      prompt1.setId(UUID.randomUUID());
      prompt1.setName("Prompt 1");

      PromptSummaryResDto prompt2 = new PromptSummaryResDto();
      prompt2.setId(UUID.randomUUID());
      prompt2.setName("Prompt 2");

      List<PromptSummaryResDto> promptsList = Arrays.asList(prompt1, prompt2);
      PromptsListDtoRes response = new PromptsListDtoRes(promptsList);

      when(customPromptService.findAllByUserId(any(UUID.class))).thenReturn(response);

      // Act & Assert
      mockMvc.perform(get("/api/custom-prompts/all"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.prompts").isArray())
          .andExpect(jsonPath("$.prompts.length()").value(2))
          .andExpect(jsonPath("$.prompts[0].name").value("Prompt 1"))
          .andExpect(jsonPath("$.prompts[1].name").value("Prompt 2"));

      verify(customPromptService, times(1)).findAllByUserId(any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/custom-prompts/all - Should return empty list when no prompts")
    void testGetAllPromptsEmpty() throws Exception {
      // Arrange
      PromptsListDtoRes response = new PromptsListDtoRes(Collections.emptyList());
      when(customPromptService.findAllByUserId(any(UUID.class))).thenReturn(response);

      // Act & Assert
      mockMvc.perform(get("/api/custom-prompts/all"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.prompts").isArray())
          .andExpect(jsonPath("$.prompts.length()").value(0));

      verify(customPromptService, times(1)).findAllByUserId(any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/custom-prompts/{promptId} - Should return prompt by id")
    void testGetPromptById() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      when(customPromptService.findByIdAndUserId(eq(promptIdString), any(UUID.class)))
          .thenReturn(Optional.of(customPrompt));

      // Act & Assert
      mockMvc.perform(get("/api/custom-prompts/{promptId}", promptIdString))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Test Prompt"))
          .andExpect(jsonPath("$.content").value("You are a helpful assistant"));

      verify(customPromptService, times(1)).findByIdAndUserId(eq(promptIdString), any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/custom-prompts/{promptId} - Should return 404 when prompt not found")
    void testGetPromptByIdNotFound() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      when(customPromptService.findByIdAndUserId(eq(promptIdString), any(UUID.class)))
          .thenReturn(Optional.empty());

      // Act & Assert
      mockMvc.perform(get("/api/custom-prompts/{promptId}", promptIdString))
          .andExpect(status().isNotFound());

      verify(customPromptService, times(1)).findByIdAndUserId(eq(promptIdString), any(UUID.class));
    }
  }

  @Nested
  @DisplayName("PATCH Endpoints")
  class PatchEndpoints {

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should update prompt successfully")
    void testUpdatePromptSuccess() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      var updateReq = new dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq();
      updateReq.setName("Updated Prompt");
      updateReq.setContent("Updated content");

      doNothing().when(customPromptService).update(eq(promptIdString), any(), any(UUID.class));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateReq)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Prompt updated successfully"));

      verify(customPromptService, times(1)).update(eq(promptIdString), any(), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return 404 when prompt not found")
    void testUpdatePromptNotFound() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      var updateReq = new dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq();
      updateReq.setName("Updated Prompt");
      updateReq.setContent("Updated content");

      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"))
          .when(customPromptService).update(eq(promptIdString), any(), any(UUID.class));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateReq)))
          .andExpect(status().isNotFound());

      verify(customPromptService, times(1)).update(eq(promptIdString), any(), any(UUID.class));
    }
  }

  @Nested
  @DisplayName("POST Endpoints")
  class PostEndpoints {

    @Test
    @DisplayName("POST /api/custom-prompts - Should create a new custom prompt")
    void testCreateCustomPrompt() throws Exception {
      // Arrange
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), any(String.class)))
          .thenReturn(customPrompt);

      // Act & Assert
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Test Prompt"))
          .andExpect(jsonPath("$.content").value("You are a helpful assistant"));

      verify(customPromptService, times(1)).create(any(CreateCustomPromptDtoReq.class), any(String.class));
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Should create a new custom prompt with messages")
    void testCreateCustomPromptWithMessages() throws Exception {
      // Arrange
      var messages = new CreateCustomPromptMessagesDto();
      messages.setRole("Assistant");
      messages.setContent("How can I help you?");
      createRequest.setMessages(Arrays.asList(messages));

      var promptMessage = PromptMessage.builder()
          .role("Assistant")
          .content("How can I help you?")
          .prompt(customPrompt)
          .build();

      customPrompt.setMessages(Collections.singletonList(promptMessage));

      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), any(String.class)))
          .thenReturn(customPrompt);

      // Act & Assert
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Test Prompt"))
          .andExpect(jsonPath("$.content").value("You are a helpful assistant"));

      verify(customPromptService, times(1)).create(any(CreateCustomPromptDtoReq.class), any(String.class));
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid Role message")
    void testCreateCustomPromptWithInvalidRoleMessage() throws Exception {
      // Arrange
      var messages = new CreateCustomPromptMessagesDto();
      messages.setRole("InvalidRole");
      messages.setContent("How can I help you?");
      createRequest.setMessages(Arrays.asList(messages));

      // Act & Assert
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());

      verify(customPromptService, never()).create(any(CreateCustomPromptDtoReq.class), any(String.class));
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Should handle service exceptions")
    void testCreateCustomPromptServiceException() throws Exception {
      // Arrange
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), any(String.class)))
          .thenThrow(new RuntimeException("Service error"));

      // Act & Assert
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isInternalServerError());

      verify(customPromptService, times(1)).create(any(CreateCustomPromptDtoReq.class), any(String.class));
    }
  }

  @Nested
  @DisplayName("DELETE Endpoints")
  class DeleteEndpoints {

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should delete prompt successfully")
    void testDeletePromptSuccess() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      doNothing().when(customPromptService).delete(eq(promptIdString), any(UUID.class));

      // Act & Assert
      mockMvc.perform(delete("/api/custom-prompts/{promptId}/delete", promptId))
          .andExpect(status().isOk());

      verify(customPromptService, times(1)).delete(eq(promptIdString), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should return 500 when service throws exception")
    void testDeletePromptServiceException() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"))
          .when(customPromptService).delete(eq(promptIdString), any(UUID.class));

      // Act & Assert
      mockMvc.perform(delete("/api/custom-prompts/{promptId}/delete", promptId))
          .andExpect(status().isNotFound());

      verify(customPromptService, times(1)).delete(eq(promptIdString), any(UUID.class));
    }
  }

  @Nested
  @DisplayName("DELETE Message Endpoints")
  class DeleteMessageEndpoints {

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should delete message successfully")
    void testDeleteMessageSuccess() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UUID messageId = UUID.randomUUID();
      doNothing().when(customPromptService).deleteMessage(eq(promptIdString), eq(messageId.toString()), any(UUID.class));

      // Act & Assert
      mockMvc.perform(delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptId, messageId))
          .andExpect(status().isOk());

      verify(customPromptService, times(1)).deleteMessage(eq(promptIdString), eq(messageId.toString()), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should return 404 when service throws not found")
    void testDeleteMessageNotFound() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UUID messageId = UUID.randomUUID();
      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"))
          .when(customPromptService).deleteMessage(eq(promptIdString), eq(messageId.toString()), any(UUID.class));

      // Act & Assert
      mockMvc.perform(delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptId, messageId))
          .andExpect(status().isNotFound());

      verify(customPromptService, times(1)).deleteMessage(eq(promptIdString), eq(messageId.toString()), any(UUID.class));
    }
  }
}
