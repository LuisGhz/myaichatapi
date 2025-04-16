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
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptMessagesDto;
import dev.luisghtz.myaichat.prompts.dtos.PromptsListDtoRes;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptMessage;
import dev.luisghtz.myaichat.prompts.entities.PromptParam;
import dev.luisghtz.myaichat.prompts.services.CustomPromptService;

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
  @DisplayName("GET /api/custom-prompts - Should return all prompts")
  void testGetAllPrompts() throws Exception {
    // Prepare mock data
    CustomPrompt promptDto1 = new CustomPrompt();
    promptDto1.setId(UUID.randomUUID());
    promptDto1.setName("Prompt 1");

    CustomPrompt promptDto2 = new CustomPrompt();
    promptDto2.setId(UUID.randomUUID());
    promptDto2.setName("Prompt 2");

    List<CustomPrompt> prompts = Arrays.asList(promptDto1, promptDto2);
    PromptsListDtoRes response = new PromptsListDtoRes();
    response.setPrompts(prompts);

    // Setup mock service
    when(customPromptService.findAll()).thenReturn(response);

    // Execute and verify
    mockMvc.perform(get("/api/custom-prompts")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.prompts").isArray())
        .andExpect(jsonPath("$.prompts.length()").value(2))
        .andExpect(jsonPath("$.prompts[0].name").value("Prompt 1"))
        .andExpect(jsonPath("$.prompts[1].name").value("Prompt 2"));
  }

  @Test
  @DisplayName("GET /api/custom-prompts - Should return empty list when no prompts")
  void testGetAllPromptsWhenEmpty() throws Exception {
    // Prepare empty response
    PromptsListDtoRes emptyResponse = new PromptsListDtoRes();
    emptyResponse.setPrompts(Collections.emptyList());

    // Setup mock service
    when(customPromptService.findAll()).thenReturn(emptyResponse);

    // Execute and verify
    mockMvc.perform(get("/api/custom-prompts")
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
  @DisplayName("POST /api/custom-prompt - Invalid Role message")
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
  @DisplayName("POST /api/custom-prompt - Invalid content message")
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
  @DisplayName("POST /api/custom-prompt - Invalid param name")
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
  @DisplayName("POST /api/custom-prompt - Invalid param value")
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
}