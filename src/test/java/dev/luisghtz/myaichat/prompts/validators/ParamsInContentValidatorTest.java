package dev.luisghtz.myaichat.prompts.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;
import jakarta.validation.ConstraintValidatorContext;

class ParamsInContentValidatorTest {

  private ParamsInContentValidator validator;

  @Mock
  private ConstraintValidatorContext context;

  @Mock
  private CreateCustomPromptDtoReq request;

  @Mock
  private CreateCustomPromptParamsDto param1;

  @Mock
  private CreateCustomPromptParamsDto param2;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new ParamsInContentValidator();
  }

  @Test
  void isValid_WhenParamsIsNull_ShouldReturnTrue() {
    when(request.getParams()).thenReturn(null);

    boolean result = validator.isValid(request, context);

    assertTrue(result);
  }

  @Test
  void isValid_WhenParamsIsEmpty_ShouldReturnTrue() {
    when(request.getParams()).thenReturn(new ArrayList<>());

    boolean result = validator.isValid(request, context);

    assertTrue(result);
  }

  @Test
  void isValid_WhenAllParamsExistInContent_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1, param2);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}, your age is {age}");
    when(param1.getName()).thenReturn("name");
    when(param2.getName()).thenReturn("age");

    boolean result = validator.isValid(request, context);

    assertTrue(result);
  }

  @Test
  void isValid_WhenParamIsEmpty_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1, param2);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}, your age is {age}");
    when(param1.getName()).thenReturn("");
    when(param2.getName()).thenReturn("age");

    boolean result = validator.isValid(request, context);

    assertTrue(result);
  }

  @Test
  void isValid_WhenSomeParamsMissingFromContent_ShouldReturnFalse() {
    List<CreateCustomPromptParamsDto> params = List.of(param1, param2);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}");
    when(param1.getName()).thenReturn("name");
    when(param2.getName()).thenReturn("age");

    boolean result = validator.isValid(request, context);

    assertFalse(result);
  }

  @Test
  void isValid_WhenParamExistsWithoutBraces_ShouldReturnFalse() {
    List<CreateCustomPromptParamsDto> params = List.of(param1);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello name");
    when(param1.getName()).thenReturn("name");

    boolean result = validator.isValid(request, context);

    assertFalse(result);
  }

  @Test
  void isValid_WhenSingleParamExists_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}!");
    when(param1.getName()).thenReturn("name");

    boolean result = validator.isValid(request, context);

    assertTrue(result);
  }

  @Test
  void isValid_WhenContentIsEmpty_ShouldReturnFalse() {
    List<CreateCustomPromptParamsDto> params = List.of(param1);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("");
    when(param1.getName()).thenReturn("name");

    boolean result = validator.isValid(request, context);

    assertFalse(result);
  }
}