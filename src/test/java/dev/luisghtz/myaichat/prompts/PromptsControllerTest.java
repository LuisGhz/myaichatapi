package dev.luisghtz.myaichat.prompts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.luisghtz.myaichat.configurationMock.AIModelsControllerTestConfiguration;
import dev.luisghtz.myaichat.configurationMock.jwt.JwtPermitAllTestConfiguration;
import dev.luisghtz.myaichat.configurationMock.UserJwtDataTestConfiguration;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.entities.PromptParam;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdatedCustomPromptDtoRes;

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

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CustomPromptService customPromptService;

  private CreateCustomPromptDtoReq createRequest;
  private CustomPrompt customPrompt;
  private UUID promptId;
  private UUID existingParamId; // Add ID for existing param
  private UUID userId; // Add user ID for tests

  @BeforeEach
  void setUp() {
    promptId = UUID.randomUUID();
    existingParamId = UUID.randomUUID(); // Initialize existing param ID
    userId = UUID.randomUUID(); // Initialize user ID

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

  private void addDefaultParams() {
    var params = new CreateCustomPromptParamsDto();
    params.setName("language");
    params.setValue("English");
    createRequest.setParams(Arrays.asList(params));
  }

  private void addDefaultMessages() {
    var messages = new CreateCustomPromptMessagesDto();
    messages.setRole("User");
    messages.setContent("Hello!");
    createRequest.setMessages(Arrays.asList(messages));
  }

  private UserJwtDataDto createTestUserJwtData() {
    UserJwtDataDto user = new UserJwtDataDto();
    user.setId("550e8400-e29b-41d4-a716-446655440000");
    user.setEmail("testuser@example.com");
    user.setUsername("testuser");
    return user;
  }

  @Nested
  @DisplayName("GET Endpoints")
  class GetEndpoints {

    @Test
    @DisplayName("GET /api/custom-prompts/all - Should return all prompts")
    void testGetAllPrompts() throws Exception {
      // Prepare mock data
      PromptSummaryResDto promptDto1 = new PromptSummaryResDto();
      promptDto1.setId(UUID.randomUUID());
      promptDto1.setName("Prompt 1");

      PromptSummaryResDto promptDto2 = new PromptSummaryResDto();
      promptDto2.setId(UUID.randomUUID());
      promptDto2.setName("Prompt 2");

      List<PromptSummaryResDto> prompts = Arrays.asList(promptDto1, promptDto2);
      PromptsListDtoRes response = new PromptsListDtoRes();
      response.setPrompts(prompts);

      // Setup mock service
      when(customPromptService.findAllByUserId(any(UUID.class))).thenReturn(response);

      // Execute and verify
      mockMvc.perform(get("/api/custom-prompts/all")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.prompts").isArray())
          .andExpect(jsonPath("$.prompts.length()").value(2))
          .andExpect(jsonPath("$.prompts[0].name").value("Prompt 1"))
          .andExpect(jsonPath("$.prompts[1].name").value("Prompt 2"));
    }

    @Test
    @DisplayName("GET /api/custom-prompts/all - Should return empty list when no prompts")
    void testGetAllPromptsWhenEmpty() throws Exception {
      // Prepare empty response
      PromptsListDtoRes emptyResponse = new PromptsListDtoRes();
      emptyResponse.setPrompts(Collections.emptyList());

      // Setup mock service
      when(customPromptService.findAllByUserId(any(UUID.class))).thenReturn(emptyResponse);

      // Execute and verify
      mockMvc.perform(get("/api/custom-prompts/all")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.prompts").isArray())
          .andExpect(jsonPath("$.prompts.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/custom-prompts/{promptId} - Should return prompt by id")
    void testGetPromptById() throws Exception {
      // Setup mock service
      when(customPromptService.findByIdAndUserId(anyString(), any(UUID.class)))
          .thenReturn(Optional.of(customPrompt));

      // Execute and verify
      mockMvc.perform(get("/api/custom-prompts/{promptId}", promptId.toString())
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(promptId.toString()))
          .andExpect(jsonPath("$.name").value("Test Prompt"))
          .andExpect(jsonPath("$.content").value("You are a helpful assistant"));
    }

    @Test
    @DisplayName("GET /api/custom-prompts/{promptId} - Should return 404 when prompt not found")
    void testGetPromptByIdNotFound() throws Exception {
      // Setup mock service
      when(customPromptService.findByIdAndUserId(anyString(), any(UUID.class)))
          .thenReturn(Optional.empty());

      // Execute and verify
      mockMvc.perform(get("/api/custom-prompts/{promptId}", promptId.toString())
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST Endpoints")
  class PostEndpoints {

    @Test
    @DisplayName("POST /api/custom-prompts - Should create a new custom prompt")
    void testCreateCustomPrompt() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);

      // Execute and verify
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(promptId.toString()))
          .andExpect(jsonPath("$.name").value("Test Prompt"))
          .andExpect(jsonPath("$.content").value("You are a helpful assistant"));
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Should create a new custom prompt with params and messages")
    void testCreatePromptWithParamsAndMessages() {
      var params = new CreateCustomPromptParamsDto();
      params.setName("language");
      params.setValue("English");
      createRequest.setParams(Collections.singletonList(params));
      var messages = new CreateCustomPromptMessagesDto();
      messages.setRole("User");
      messages.setContent("Hello!");
      createRequest.setMessages(Collections.singletonList(messages));
      createRequest.setContent("You are a helpful assistant in {{language}}.");

      var promptParam = PromptParam.builder()
          .id(existingParamId)
          .name("language")
          .value("English")
          .build();

      var promptMessage = PromptMessage.builder()
          .role("User")
          .content("Hello!")
          .build();
      customPrompt.setParams(Collections.singletonList(promptParam));
      customPrompt.setMessages(Collections.singletonList(promptMessage));
      customPrompt.setContent("You are a helpful assistant in English.");

      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      try {
        // Execute and verify
        mockMvc.perform(post("/api/custom-prompts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(promptId.toString()))
            .andExpect(jsonPath("$.name").value("Test Prompt"))
            .andExpect(jsonPath("$.content").value("You are a helpful assistant in English."))
            .andExpect(jsonPath("$.params[0].name").value("language"))
            .andExpect(jsonPath("$.params[0].value").value("English"))
            .andExpect(jsonPath("$.messages[0].role").value("User"))
            .andExpect(jsonPath("$.messages[0].content").value("Hello!"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Should not create a new custom prompt with params not in content")
    public void testNotCreateCustomPromptWithInvalidParam() throws Exception {
      var params = new CreateCustomPromptParamsDto();
      params.setName("Language");
      params.setValue("English");

      createRequest.setParams(Arrays.asList(params));
      createRequest.setContent("You are a helpful assistant in language.");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid Role message")
    void testInvalidRoleMessage() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultMessages();
      createRequest.getMessages().get(0).setRole("InvalidRole");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid Role message empty")
    void testInvalidRoleMessageEmpty() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultMessages();
      createRequest.getMessages().get(0).setRole("");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid Role message empty trim")
    void testInvalidRoleMessageEmptyTrim() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultMessages();
      createRequest.getMessages().get(0).setRole("   ");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid content message")
    void testInvalidContentMessage() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultMessages();
      createRequest.getMessages().get(0).setContent("");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid content message trim")
    void testInvalidContentMessageTrim() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultMessages();
      createRequest.getMessages().get(0).setContent("   ");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid content message (max length)")
    void testInvalidContentMessageMaxLength() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultMessages();
      createRequest.getMessages().get(0).setContent("a".repeat(10_001)); // Exceeds max length
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid param name empty")
    void testInvalidParamNameEmpty() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultParams();
      createRequest.getParams().get(0).setName("");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid param name empty trim")
    void testInvalidParamNameEmptyTrim() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultParams();
      createRequest.getParams().get(0).setName("   ");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid param name (max length)")
    void testInvalidParamNameMaxLength() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultParams();
      createRequest.getParams().get(0).setName("a".repeat(16)); // Exceeds max length
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid param value")
    void testInvalidParamValue() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultParams();
      createRequest.getParams().get(0).setValue("  ");
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid param value (max length)")
    void testInvalidParamValueMaxLength() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      addDefaultParams();
      createRequest.getParams().get(0).setValue("a".repeat(101)); // Exceeds max length
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid prompt name (max length)")
    void testInvalidPromptNameMaxLength() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      createRequest.setName("a".repeat(31)); // Exceeds max length
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Invalid prompt content (max length)")
    void testInvalidPromptContentMaxLength() throws Exception {
      // Setup mock service
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString())).thenReturn(customPrompt);
      createRequest.setContent("a".repeat(10_001)); // Exceeds max length
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/custom-prompts - Should handle service exceptions")
    void testCreateCustomPromptWithServiceException() throws Exception {
      // Setup mock service to throw exception
      when(customPromptService.create(any(CreateCustomPromptDtoReq.class), anyString()))
          .thenThrow(new RuntimeException("Service error"));

      // Execute and verify
      mockMvc.perform(post("/api/custom-prompts")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createRequest)))
          .andExpect(status().isInternalServerError());
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
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Updated Prompt"); // Shortened name
      updateRequest.setContent("Updated content");

      UpdateCustomPromptParamsDto updatedParam = new UpdateCustomPromptParamsDto();
      updatedParam.setName("max_tokens");
      updatedParam.setValue("150");
      updateRequest.setParams(Arrays.asList(updatedParam));

      UpdateCustomPromptMessagesDto updatedMessage = new UpdateCustomPromptMessagesDto();
      updatedMessage.setRole("User");
      updatedMessage.setContent("You are an updated assistant.");
      updateRequest.setMessages(Arrays.asList(updatedMessage));

      // Mock service call
      doNothing().when(customPromptService).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));

      // Expected response
      UpdatedCustomPromptDtoRes expectedResponse = new UpdatedCustomPromptDtoRes("Prompt updated successfully");

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          // Expect the new response DTO as JSON
          .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

      // Verify service interaction
      verify(customPromptService, times(1)).update(eq(promptIdString), eq(updateRequest), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Not Found for non-existing prompt")
    void testUpdatePromptNotFound() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Updated Prompt");
      updateRequest.setContent("Updated content");

      // Mock service call to throw exception
      doThrow(new AppNotFoundException("Prompt not found"))
          .when(customPromptService).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isNotFound());

      // Verify service interaction
      verify(customPromptService, times(1)).update(eq(promptIdString), eq(updateRequest), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid data (e.g., blank name)")
    void testUpdatePromptInvalidData() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq invalidUpdateRequest = new UpdateCustomPromptDtoReq();
      invalidUpdateRequest.setName(" "); // Assuming @NotBlank validation
      invalidUpdateRequest.setContent("Valid content"); // Need valid content to isolate name error

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid prompt name (max length)")
    void testUpdatePromptInvalidNameMaxLength() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("a".repeat(31)); // Exceeds max length
      updateRequest.setContent("Valid content");

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid prompt content (max length)")
    void testUpdatePromptInvalidContentMaxLength() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Valid Name");
      updateRequest.setContent("a".repeat(10_001)); // Exceeds max length 10000

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid message role")
    void testUpdatePromptInvalidMessageRole() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Valid Name");
      updateRequest.setContent("Valid Content"); // Add valid content

      UpdateCustomPromptMessagesDto invalidMessage = new UpdateCustomPromptMessagesDto();
      invalidMessage.setRole("InvalidRole"); // Assuming validation like @Pattern(regexp = "User|System|Assistant")
      invalidMessage.setContent("Valid content");
      updateRequest.setMessages(Arrays.asList(invalidMessage));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid message content (max length)")
    void testUpdatePromptInvalidMessageContentMaxLength() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Valid Name");
      updateRequest.setContent("Valid Content");

      UpdateCustomPromptMessagesDto invalidMessage = new UpdateCustomPromptMessagesDto();
      invalidMessage.setRole("User");
      invalidMessage.setContent("a".repeat(10_001)); // Exceeds max length 10000
      updateRequest.setMessages(Arrays.asList(invalidMessage));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid param name")
    void testUpdatePromptInvalidParamName() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Valid Name");
      updateRequest.setContent("Valid Content"); // Add valid content

      UpdateCustomPromptParamsDto invalidParam = new UpdateCustomPromptParamsDto();
      invalidParam.setName(" "); // Assuming @NotBlank validation
      invalidParam.setValue("Valid value");
      updateRequest.setParams(Arrays.asList(invalidParam));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid param name (max length)")
    void testUpdatePromptInvalidParamNameMaxLength() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Valid Name");
      updateRequest.setContent("Valid Content");

      UpdateCustomPromptParamsDto invalidParam = new UpdateCustomPromptParamsDto();
      invalidParam.setName("a".repeat(16)); // Exceeds max length 15
      invalidParam.setValue("Valid value");
      updateRequest.setParams(Arrays.asList(invalidParam));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid param value (max length)")
    void testUpdatePromptInvalidParamValueMaxLength() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Valid Name");
      updateRequest.setContent("Valid Content");

      UpdateCustomPromptParamsDto invalidParam = new UpdateCustomPromptParamsDto();
      invalidParam.setName("Valid Param Name");
      invalidParam.setValue("a".repeat(101)); // Exceeds max length 100
      updateRequest.setParams(Arrays.asList(invalidParam));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isBadRequest());

      // Verify service interaction (should not be called)
      verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));
    }

    @Test
    @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should handle service exceptions")
    void testUpdatePromptServiceException() throws Exception {
      // Arrange
      String promptIdString = promptId.toString();
      UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
      updateRequest.setName("Updated Prompt");
      updateRequest.setContent("Updated content");
      // Add valid params/messages if needed for the request to be valid before
      // hitting the service
      updateRequest.setParams(Collections.emptyList());
      updateRequest.setMessages(Collections.emptyList());

      // Mock service call to throw exception
      doThrow(new RuntimeException("Service error"))
          .when(customPromptService).update(anyString(), any(UpdateCustomPromptDtoReq.class), any(UUID.class));

      // Act & Assert
      mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isInternalServerError());

      // Verify service interaction
      verify(customPromptService, times(1)).update(eq(promptIdString), eq(updateRequest), any(UUID.class));
    }
  }

  @Nested
  @DisplayName("DELETE Endpoints")
  class DeleteEndpoints {

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{paramId}/delete-param - Should delete param successfully")
    void testDeleteParamSuccess() throws Exception {
      String promptIdString = promptId.toString();
      String paramId = promptIdString;
      doNothing().when(customPromptService).deleteParam(eq(promptIdString), eq(paramId), any(UUID.class));

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{paramId}/delete-param", promptIdString, paramId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().string(""));

      verify(customPromptService, times(1)).deleteParam(eq(promptIdString), eq(paramId), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{paramId}/delete-param - Should return Internal Server Error on service exception")
    void testDeleteParamServiceException() throws Exception {
      String promptIdString = promptId.toString();
      String paramId = promptIdString;
      doThrow(new RuntimeException("Service error"))
          .when(customPromptService).deleteParam(eq(promptIdString), eq(paramId), any(UUID.class));

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{paramId}/delete-param", promptIdString, paramId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError());

      verify(customPromptService, times(1)).deleteParam(eq(promptIdString), eq(paramId), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{paramId}/delete-param - Should return Bad Request for invalid promptId")
    void testDeleteParamInvalidPromptId() throws Exception {
      String invalidPromptId = " ";
      String paramId = "param-123";

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{paramId}/delete-param", invalidPromptId, paramId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verify(customPromptService, never()).deleteParam(anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{paramId}/delete-param - Should return Bad Request for invalid paramId")
    void testDeleteParamInvalidParamId() throws Exception {
      String promptIdString = promptId.toString();
      String invalidParamId = " ";

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{paramId}/delete-param", promptIdString, invalidParamId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verify(customPromptService, never()).deleteParam(anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should delete message successfully")
    void testDeleteMessageSuccess() throws Exception {
      String promptIdString = promptId.toString();
      String messageId = UUID.randomUUID().toString();
      doNothing().when(customPromptService).deleteMessage(eq(promptIdString), eq(messageId), any(UUID.class));

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptIdString, messageId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().string(""));

      verify(customPromptService, times(1)).deleteMessage(eq(promptIdString), eq(messageId), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should return Internal Server Error on service exception")
    void testDeleteMessageServiceException() throws Exception {
      String promptIdString = promptId.toString();
      String messageId = UUID.randomUUID().toString();
      doThrow(new RuntimeException("Service error"))
          .when(customPromptService).deleteMessage(eq(promptIdString), eq(messageId), any(UUID.class));

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptIdString, messageId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError());

      verify(customPromptService, times(1)).deleteMessage(eq(promptIdString), eq(messageId), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should return Bad Request for invalid promptId")
    void testDeleteMessageInvalidPromptId() throws Exception {
      String invalidPromptId = " ";
      String messageId = UUID.randomUUID().toString();

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", invalidPromptId, messageId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verify(customPromptService, never()).deleteMessage(anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should return Bad Request for invalid messageId")
    void testDeleteMessageInvalidMessageId() throws Exception {
      String promptIdString = promptId.toString();
      String invalidMessageId = " ";

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptIdString, invalidMessageId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verify(customPromptService, never()).deleteMessage(anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should delete prompt successfully")
    void testDeletePromptSuccess() throws Exception {
      String promptIdString = promptId.toString();
      doNothing().when(customPromptService).delete(eq(promptIdString), any(UUID.class));

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/delete", promptIdString)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().string(""));

      verify(customPromptService, times(1)).delete(eq(promptIdString), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should return Internal Server Error on service exception")
    void testDeletePromptServiceException() throws Exception {
      String promptIdString = promptId.toString();
      doThrow(new RuntimeException("Service error"))
          .when(customPromptService).delete(eq(promptIdString), any(UUID.class));

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/delete", promptIdString)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError());

      verify(customPromptService, times(1)).delete(eq(promptIdString), any(UUID.class));
    }

    @Test
    @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should return Bad Request for invalid promptId")
    void testDeletePromptInvalidPromptId() throws Exception {
      String invalidPromptId = " ";

      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders
              .delete("/api/custom-prompts/{promptId}/delete", invalidPromptId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verify(customPromptService, never()).delete(anyString(), any(UUID.class));
    }
  }
}