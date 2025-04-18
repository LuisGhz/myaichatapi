package dev.luisghtz.myaichat.prompts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptParamsDto;

@WebMvcTest(PromptsController.class)
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

  @BeforeEach
  void setUp() {
    promptId = UUID.randomUUID();

    // Setup request DTO
    createRequest = new CreateCustomPromptDtoReq();
    createRequest.setName("Test Prompt");
    createRequest.setContent("You are a helpful assistant");
    // Add params and messages for new DTO structure
    CreateCustomPromptParamsDto param = new CreateCustomPromptParamsDto();
    param.setName("temperature");
    param.setValue("0.7");
    createRequest.setParams(Arrays.asList(param));
    CreateCustomPromptMessagesDto message = new CreateCustomPromptMessagesDto();
    message.setRole("User");
    message.setContent("Hello!");
    createRequest.setMessages(Arrays.asList(message));

    // Setup entity response
    var promptParam = PromptParam.builder()
        .name("temperature")
        .value("0.7")
        .build();
    var promptMessage = PromptMessage.builder()
        .role("User")
        .content("Hello!")
        .build();

    customPrompt = CustomPrompt.builder()
        .id(promptId)
        .name("Test Prompt")
        .content("You are a helpful assistant")
        .params(List.of(promptParam))
        .messages(List.of(promptMessage))
        .build();
  }

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
    when(customPromptService.findAll()).thenReturn(response);

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
    when(customPromptService.findAll()).thenReturn(emptyResponse);

    // Execute and verify
    mockMvc.perform(get("/api/custom-prompts/all")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.prompts").isArray())
        .andExpect(jsonPath("$.prompts.length()").value(0));
  }

  @Test
  @DisplayName("POST /api/custom-prompts - Should create a new custom prompt")
  void testCreateCustomPrompt() throws Exception {
    // Setup mock service
    when(customPromptService.create(any(CreateCustomPromptDtoReq.class))).thenReturn(customPrompt);

    // Execute and verify
    mockMvc.perform(post("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(promptId.toString()))
        .andExpect(jsonPath("$.name").value("Test Prompt"))
        .andExpect(jsonPath("$.content").value("You are a helpful assistant"))
        .andExpect(jsonPath("$.params").isArray())
        .andExpect(jsonPath("$.params[0].name").value("temperature"))
        .andExpect(jsonPath("$.params[0].value").value("0.7"))
        .andExpect(jsonPath("$.messages").isArray())
        .andExpect(jsonPath("$.messages[0].role").value("User"))
        .andExpect(jsonPath("$.messages[0].content").value("Hello!"));
  }

  @Test
  @DisplayName("POST /api/custom-prompts - Invalid Role message")
  void testInvalidRoleMessage() throws Exception {
    // Setup mock service
    when(customPromptService.create(any(CreateCustomPromptDtoReq.class))).thenReturn(customPrompt);
    createRequest.getMessages().get(0).setRole("InvalidRole");
    mockMvc.perform(post("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/custom-prompts - Invalid content message")
  void testInvalidContentMessage() throws Exception {
    // Setup mock service
    when(customPromptService.create(any(CreateCustomPromptDtoReq.class))).thenReturn(customPrompt);
    createRequest.getMessages().get(0).setContent("");
    mockMvc.perform(post("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/custom-prompts - Invalid param name")
  void testInvalidParamName() throws Exception {
    // Setup mock service
    when(customPromptService.create(any(CreateCustomPromptDtoReq.class))).thenReturn(customPrompt);
    createRequest.getParams().get(0).setName("  ");
    ;
    mockMvc.perform(post("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/custom-prompts - Invalid param value")
  void testInvalidParamValue() throws Exception {
    // Setup mock service
    when(customPromptService.create(any(CreateCustomPromptDtoReq.class))).thenReturn(customPrompt);
    createRequest.getParams().get(0).setValue("  ");
    ;
    mockMvc.perform(post("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/custom-prompts - Should handle service exceptions")
  void testCreateCustomPromptWithServiceException() throws Exception {
    // Setup mock service to throw exception
    when(customPromptService.create(any(CreateCustomPromptDtoReq.class)))
        .thenThrow(new RuntimeException("Service error"));

    // Execute and verify
    mockMvc.perform(post("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should update prompt successfully")
  void testUpdatePromptSuccess() throws Exception {
    // Arrange
    String promptIdString = promptId.toString();
    UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
    updateRequest.setName("Updated Test Prompt");
    updateRequest.setContent("Updated content");

    UpdateCustomPromptParamsDto updatedParam = new UpdateCustomPromptParamsDto();
    updatedParam.setName("max_tokens");
    updatedParam.setValue("150");
    updateRequest.setParams(Arrays.asList(updatedParam));

    UpdateCustomPromptMessagesDto updatedMessage = new UpdateCustomPromptMessagesDto();
    updatedMessage.setRole("Assistant");
    updatedMessage.setContent("You are an updated assistant.");
    updateRequest.setMessages(Arrays.asList(updatedMessage));

    // Mock service call
    doNothing().when(customPromptService).update(anyString(), any(UpdateCustomPromptDtoReq.class));
    // Act & Assert
    mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Prompt updated successfully"));

    // Verify service interaction
    verify(customPromptService, times(1)).update(promptIdString, updateRequest);
  }

  @Test
  @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Not Found for non-existing prompt")
  void testUpdatePromptNotFound() throws Exception {
    // Arrange
    String promptIdString = promptId.toString();
    UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
    updateRequest.setName("Updated Test Prompt");
    updateRequest.setContent("Updated content");

    // Mock service call to throw exception
    doThrow(new AppNotFoundException("Prompt not found"))
        .when(customPromptService).update(anyString(), any(UpdateCustomPromptDtoReq.class));

    // Act & Assert
    mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());

    // Verify service interaction
    verify(customPromptService, times(1)).update(promptIdString, updateRequest);
  }

  @Test
  @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid data (e.g., blank name)")
  void testUpdatePromptInvalidData() throws Exception {
    // Arrange
    String promptIdString = promptId.toString();
    UpdateCustomPromptDtoReq invalidUpdateRequest = new UpdateCustomPromptDtoReq();
    invalidUpdateRequest.setName(" "); // Assuming @NotBlank validation

    // Act & Assert
    mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
        .andExpect(status().isBadRequest());

    // Verify service interaction (should not be called)
    verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class));
  }

   @Test
   @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid message role")
   void testUpdatePromptInvalidMessageRole() throws Exception {
       // Arrange
       String promptIdString = promptId.toString();
       UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
       updateRequest.setName("Valid Name");

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
       verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class));
   }

   @Test
   @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should return Bad Request for invalid param name")
   void testUpdatePromptInvalidParamName() throws Exception {
       // Arrange
       String promptIdString = promptId.toString();
       UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
       updateRequest.setName("Valid Name");

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
       verify(customPromptService, never()).update(anyString(), any(UpdateCustomPromptDtoReq.class));
   }


  @Test
  @DisplayName("PATCH /api/custom-prompts/{promptId}/update - Should handle service exceptions")
  void testUpdatePromptServiceException() throws Exception {
    // Arrange
    String promptIdString = promptId.toString();
    UpdateCustomPromptDtoReq updateRequest = new UpdateCustomPromptDtoReq();
    updateRequest.setName("Updated Test Prompt");
    updateRequest.setContent("Updated content");
    // Add valid params/messages if needed for the request to be valid before hitting the service
    updateRequest.setParams(Collections.emptyList());
    updateRequest.setMessages(Collections.emptyList());


    // Mock service call to throw exception
    doThrow(new RuntimeException("Service error"))
        .when(customPromptService).update(anyString(), any(UpdateCustomPromptDtoReq.class));

    // Act & Assert
    mockMvc.perform(patch("/api/custom-prompts/{promptId}/update", promptIdString)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isInternalServerError());

    // Verify service interaction
    verify(customPromptService, times(1)).update(promptIdString, updateRequest);
  }
  @Test
  @DisplayName("DELETE /api/custom-prompts/{promptId}/{paramId}/delete-param - Should delete param successfully")
  void testDeleteParamSuccess() throws Exception {
    String promptIdString = promptId.toString();
    String paramId = promptIdString;
    doNothing().when(customPromptService).deleteParam(promptIdString, paramId);

    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/custom-prompts/{promptId}/{paramId}/delete-param", promptIdString, paramId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    verify(customPromptService, times(1)).deleteParam(promptIdString, paramId);
  }

  @Test
  @DisplayName("DELETE /api/custom-prompts/{promptId}/{paramId}/delete-param - Should return Internal Server Error on service exception")
  void testDeleteParamServiceException() throws Exception {
    String promptIdString = promptId.toString();
    String paramId = promptIdString;
    doThrow(new RuntimeException("Service error"))
        .when(customPromptService).deleteParam(promptIdString, paramId);

    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/custom-prompts/{promptId}/{paramId}/delete-param", promptIdString, paramId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

    verify(customPromptService, times(1)).deleteParam(promptIdString, paramId);
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

    verify(customPromptService, never()).deleteParam(anyString(), anyString());
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

    verify(customPromptService, never()).deleteParam(anyString(), anyString());
  }

  @Test
  @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should delete message successfully")
  void testDeleteMessageSuccess() throws Exception {
    String promptIdString = promptId.toString();
    String messageId = UUID.randomUUID().toString();
    doNothing().when(customPromptService).deleteMessage(promptIdString, messageId);

    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptIdString, messageId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    verify(customPromptService, times(1)).deleteMessage(promptIdString, messageId);
  }

  @Test
  @DisplayName("DELETE /api/custom-prompts/{promptId}/{messageId}/delete-message - Should return Internal Server Error on service exception")
  void testDeleteMessageServiceException() throws Exception {
    String promptIdString = promptId.toString();
    String messageId = UUID.randomUUID().toString();
    doThrow(new RuntimeException("Service error"))
        .when(customPromptService).deleteMessage(promptIdString, messageId);

    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/custom-prompts/{promptId}/{messageId}/delete-message", promptIdString, messageId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

    verify(customPromptService, times(1)).deleteMessage(promptIdString, messageId);
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

    verify(customPromptService, never()).deleteMessage(anyString(), anyString());
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

    verify(customPromptService, never()).deleteMessage(anyString(), anyString());
  }

  @Test
  @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should delete prompt successfully")
  void testDeletePromptSuccess() throws Exception {
    String promptIdString = promptId.toString();
    doNothing().when(customPromptService).delete(promptIdString);

    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/custom-prompts/{promptId}/delete", promptIdString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    verify(customPromptService, times(1)).delete(promptIdString);
  }

  @Test
  @DisplayName("DELETE /api/custom-prompts/{promptId}/delete - Should return Internal Server Error on service exception")
  void testDeletePromptServiceException() throws Exception {
    String promptIdString = promptId.toString();
    doThrow(new RuntimeException("Service error"))
        .when(customPromptService).delete(promptIdString);

    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .delete("/api/custom-prompts/{promptId}/delete", promptIdString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

    verify(customPromptService, times(1)).delete(promptIdString);
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

    verify(customPromptService, never()).delete(anyString());
  }
}