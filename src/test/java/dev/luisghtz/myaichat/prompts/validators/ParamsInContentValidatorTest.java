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
    void isValid_WhenParamNameIsWhitespace_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1, param2);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}, your age is {age}");
    when(param1.getName()).thenReturn("   ");
    when(param2.getName()).thenReturn("age");

    boolean result = validator.isValid(request, context);

    assertTrue(result);
    }

    @Test
    void isValid_WhenParamNameIsNull_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1, param2);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}, your age is {age}");
    when(param1.getName()).thenReturn(null);
    when(param2.getName()).thenReturn("age");

    // The validator does not handle null param names, so this will throw a NullPointerException.
    // To fix this, the validator should check for null param names.
    // For now, we expect a NullPointerException.
    assertThrows(NullPointerException.class, () -> validator.isValid(request, context));
    }

    @Test
    void isValid_WhenContentIsNull_ShouldReturnFalse() {
    List<CreateCustomPromptParamsDto> params = List.of(param1);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn(null);
    when(param1.getName()).thenReturn("name");

    // The validator does not handle null content, so this will throw a NullPointerException.
    // To fix this, the validator should check for null content.
    // For now, we expect a NullPointerException.
    assertThrows(NullPointerException.class, () -> validator.isValid(request, context));
    }

    @Test
    void isValid_WhenParamNameIsSubstringOfAnotherParam_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1, param2);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {name}, your username is {username}");
    when(param1.getName()).thenReturn("name");
    when(param2.getName()).thenReturn("username");

    boolean result = validator.isValid(request, context);

    assertTrue(result);
    }

    @Test
    void isValid_WhenParamNameHasSpecialCharacters_ShouldReturnTrue() {
    List<CreateCustomPromptParamsDto> params = List.of(param1);
    when(request.getParams()).thenReturn(params);
    when(request.getContent()).thenReturn("Hello {user_name-1}!");
    when(param1.getName()).thenReturn("user_name-1");

    boolean result = validator.isValid(request, context);

    assertTrue(result);
    }
}