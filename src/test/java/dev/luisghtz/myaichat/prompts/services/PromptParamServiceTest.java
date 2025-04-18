package dev.luisghtz.myaichat.prompts.services;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.update.UpdateCustomPromptParamsDto;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import dev.luisghtz.myaichat.prompts.entities.PromptParam;
import dev.luisghtz.myaichat.prompts.repositories.PromptParamRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromptParamServiceTest {

  @Mock
  private PromptParamRepository promptParamRepository;

  @InjectMocks
  private PromptParamService promptParamService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    promptParamService = new PromptParamService(promptParamRepository);
  }

  @Test
  void testAddPromptParamsIfExists_NullOrEmpty() {
    CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    dto.setParams(null);
    promptParamService.addPromptParamsIfExists(dto, prompt);
    assertNull(prompt.getParams());

    dto.setParams(Collections.emptyList());
    promptParamService.addPromptParamsIfExists(dto, prompt);
    assertNull(prompt.getParams());
  }

  @Test
  void testAddPromptParamsIfExists_ValidParams() {
    CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    CreateCustomPromptParamsDto paramDto = new CreateCustomPromptParamsDto();
    paramDto.setName("foo");
    paramDto.setValue("bar");
    dto.setParams(List.of(paramDto));

    promptParamService.addPromptParamsIfExists(dto, prompt);

    assertNotNull(prompt.getParams());
    assertEquals(1, prompt.getParams().size());
    PromptParam param = prompt.getParams().get(0);
    assertEquals("foo", param.getName());
    assertEquals("bar", param.getValue());
    assertEquals(prompt, param.getPrompt());
  }

  @Test
  void testAddPromptParamsIfExists_InvalidNameOrValue() {
    CreateCustomPromptDtoReq dto = new CreateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    CreateCustomPromptParamsDto paramDto = new CreateCustomPromptParamsDto();
    paramDto.setName("");
    paramDto.setValue("bar");
    dto.setParams(List.of(paramDto));

    assertThrows(AppMethodArgumentNotValidException.class,
      () -> promptParamService.addPromptParamsIfExists(dto, prompt));

    paramDto.setName("foo");
    paramDto.setValue("");
    assertThrows(AppMethodArgumentNotValidException.class,
      () -> promptParamService.addPromptParamsIfExists(dto, prompt));
  }

  @Test
  void testAddPromptParamsIfExistsToExistingPrompt_NullOrEmpty() {
    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().build();

    dto.setParams(null);
    promptParamService.addPromptParamsIfExistsToExistingPrompt(dto, prompt);
    assertNull(prompt.getParams());

    dto.setParams(Collections.emptyList());
    promptParamService.addPromptParamsIfExistsToExistingPrompt(dto, prompt);
    assertNull(prompt.getParams());
  }

  @Test
  void testAddPromptParamsIfExistsToExistingPrompt_AddsNew() {
    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().params(new ArrayList<>()).build();

    UpdateCustomPromptParamsDto paramDto = new UpdateCustomPromptParamsDto();
    paramDto.setId(null);
    paramDto.setName("foo");
    paramDto.setValue("bar");
    dto.setParams(List.of(paramDto));

    promptParamService.addPromptParamsIfExistsToExistingPrompt(dto, prompt);

    assertNotNull(prompt.getParams());
    assertEquals(1, prompt.getParams().size());
    PromptParam param = prompt.getParams().get(0);
    assertEquals("foo", param.getName());
    assertEquals("bar", param.getValue());
    assertEquals(prompt, param.getPrompt());
  }

  @Test
  void testAddPromptParamsIfExistsToExistingPrompt_ThrowsOnInvalidOrDuplicate() {
    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    CustomPrompt prompt = CustomPrompt.builder().params(new ArrayList<>()).build();

    UpdateCustomPromptParamsDto paramDto = new UpdateCustomPromptParamsDto();
    paramDto.setId(null);
    paramDto.setName("");
    paramDto.setValue("bar");
    dto.setParams(List.of(paramDto));

    assertThrows(AppMethodArgumentNotValidException.class,
      () -> promptParamService.addPromptParamsIfExistsToExistingPrompt(dto, prompt));

    // Add a param with name "foo"
    paramDto.setName("foo");
    paramDto.setValue("bar");
    promptParamService.addPromptParamsIfExistsToExistingPrompt(dto, prompt);

    // Try to add another param with same name
    UpdateCustomPromptParamsDto paramDto2 = new UpdateCustomPromptParamsDto();
    paramDto2.setId(null);
    paramDto2.setName("foo");
    paramDto2.setValue("baz");
    dto.setParams(List.of(paramDto2));

    assertThrows(AppMethodArgumentNotValidException.class,
      () -> promptParamService.addPromptParamsIfExistsToExistingPrompt(dto, prompt));
  }

  @Test
  void testHandleUpdatedExistingParams_UpdatesFields() {
    UUID paramId = UUID.randomUUID();
    PromptParam existing = PromptParam.builder()
      .id(paramId)
      .name("foo")
      .value("bar")
      .build();
    CustomPrompt prompt = CustomPrompt.builder()
      .params(new ArrayList<>(List.of(existing)))
      .build();

    UpdateCustomPromptParamsDto updateDto = new UpdateCustomPromptParamsDto();
    updateDto.setId(paramId.toString());
    updateDto.setName("baz");
    updateDto.setValue("qux");

    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    dto.setParams(List.of(updateDto));

    promptParamService.handleUpdatedExistingParams(dto, prompt);

    assertEquals("baz", existing.getName());
    assertEquals("qux", existing.getValue());
  }

  @Test
  void testHandleUpdatedExistingParams_PartialUpdate() {
    UUID paramId = UUID.randomUUID();
    PromptParam existing = PromptParam.builder()
      .id(paramId)
      .name("foo")
      .value("bar")
      .build();
    CustomPrompt prompt = CustomPrompt.builder()
      .params(new ArrayList<>(List.of(existing)))
      .build();

    UpdateCustomPromptParamsDto updateDto = new UpdateCustomPromptParamsDto();
    updateDto.setId(paramId.toString());
    updateDto.setName(null);
    updateDto.setValue("newval");

    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    dto.setParams(List.of(updateDto));

    promptParamService.handleUpdatedExistingParams(dto, prompt);

    assertEquals("foo", existing.getName());
    assertEquals("newval", existing.getValue());
  }

  @Test
  void testHandleUpdatedExistingParams_ThrowsOnDuplicateName() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    PromptParam p1 = PromptParam.builder().id(id1).name("foo").value("bar").build();
    PromptParam p2 = PromptParam.builder().id(id2).name("baz").value("qux").build();
    CustomPrompt prompt = CustomPrompt.builder()
      .params(new ArrayList<>(List.of(p1, p2)))
      .build();

    UpdateCustomPromptParamsDto updateDto = new UpdateCustomPromptParamsDto();
    updateDto.setId(id2.toString());
    updateDto.setName("foo"); // duplicate name

    UpdateCustomPromptDtoReq dto = new UpdateCustomPromptDtoReq();
    dto.setParams(List.of(updateDto));

    assertThrows(AppMethodArgumentNotValidException.class,
      () -> promptParamService.handleUpdatedExistingParams(dto, prompt));
  }

  @Test
  void testDeleteByIdAndPromptId_CallsRepository() {
    String id = UUID.randomUUID().toString();
    String promptId = UUID.randomUUID().toString();

    promptParamService.deleteByIdAndPromptId(id, promptId);

    verify(promptParamRepository).deleteByIdAndPromptId(UUID.fromString(id), UUID.fromString(promptId));
  }

  @Test
  void testSaveAndFindById() {
    PromptParam param = PromptParam.builder().build();
    when(promptParamRepository.save(param)).thenReturn(param);

    assertEquals(param, promptParamService.save(param));
    UUID id = UUID.randomUUID();
    when(promptParamRepository.findById(id)).thenReturn(Optional.of(param));
    assertEquals(Optional.of(param), promptParamService.findById(id.toString()));
  }
}