package dev.luisghtz.myaichat.advices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import dev.luisghtz.myaichat.chat.dtos.ChatErrorResponseDto;
import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.exceptions.ImageNotValidException;
import dev.luisghtz.myaichat.exceptions.ResourceInUseException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;


public class GlobalControllerAdviceTest {

  private GlobalControllerAdvice globalControllerAdvice;
  private Method getErrorMessageMethod;

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    globalControllerAdvice = new GlobalControllerAdvice();
    getErrorMessageMethod = GlobalControllerAdvice.class.getDeclaredMethod("getErrorMessage", String.class);
    getErrorMessageMethod.setAccessible(true);
  }

  @Test
  void testGetErrorMessage_whenMessageMatchesPattern_shouldReturnFormattedMessage() throws Exception {
    String input = "Detail: Key (name)=(test_user) already exists.";
    String expected = "'test_user' is already in use";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testGetErrorMessage_whenMessageMatchesPatternWithDifferentName_shouldReturnFormattedMessage() throws Exception {
    String input = "Error: Key (name)=(another_value123) already exists, constraint violation.";
    String expected = "'another_value123' is already in use";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testGetErrorMessage_whenMessageMatchesPatternWithSpecialCharsInName_shouldReturnFormattedMessage() throws Exception {
    String input = "Key (name)=(user@example.com!) already exists";
    String expected = "'user@example.com!' is already in use";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testGetErrorMessage_whenMessageDoesNotContainNameKey_shouldReturnOriginalMessage() throws Exception {
    String input = "Some other unique constraint violation.";
    String expected = "Some other unique constraint violation.";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testGetErrorMessage_whenMessageContainsKeyNameButNotFullPattern_shouldReturnOriginalMessage() throws Exception {
    String input = "Key (name)= is present but pattern is not complete";
    String expected = "Key (name)= is present but pattern is not complete";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testGetErrorMessage_whenMessageIsEmpty_shouldReturnEmptyMessage() throws Exception {
    String input = "";
    String expected = "";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testGetErrorMessage_whenNameInPatternIsEmpty_shouldReturnFormattedMessageWithEmptyQuotes() throws Exception {
    String input = "Key (name)=(name) already exists";
    String expected = "'name' is already in use";
    String actual = (String) getErrorMessageMethod.invoke(globalControllerAdvice, input);
    assertEquals(expected, actual);
  }

  @Test
  void testHandleMethodArgumentNotValidException() {
    // Arrange
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("objectName", "field1", "must not be empty");
    FieldError fieldError2 = new FieldError("objectName", "field2", "must be valid");
    
    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError, fieldError2));
    
    // Act
    var response = globalControllerAdvice.handleValidationException(ex);
    
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ChatErrorResponseDto body = response.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, body.getStatusCode());
    assertTrue(body.getMessage().contains("field1: must not be empty"));
    assertTrue(body.getMessage().contains("field2: must be valid"));
  }

  @Test
  void testHandleIllegalArgumentException() {
    // Arrange
    IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
    
    // Act
    var response = globalControllerAdvice.handleValidationException(ex);
    
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ChatErrorResponseDto body = response.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, body.getStatusCode());
    assertEquals("Validation error: Invalid argument", body.getMessage());
  }

  @Test
  void testHandleAppMethodArgumentNotValidException() {
    // Arrange
    AppMethodArgumentNotValidException ex = new AppMethodArgumentNotValidException("Custom validation error");
    
    // Act
    var response = globalControllerAdvice.handleValidationException(ex);
    
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ChatErrorResponseDto body = response.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, body.getStatusCode());
    assertEquals("Validation error: Custom validation error", body.getMessage());
  }

  @Test
  void testHandleImageNotValidException() {
    // Arrange
    ImageNotValidException ex = new ImageNotValidException("Invalid image format");
    
    // Act
    var response = globalControllerAdvice.handleImageNotValidException(ex);
    
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ChatErrorResponseDto body = response.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, body.getStatusCode());
    assertEquals("Invalid image format", body.getMessage());
  }

  @Test
  void testHandleAppNotFoundException() {
    // Arrange
    AppNotFoundException ex = new AppNotFoundException("Resource not found");
    
    // Act
    var response = globalControllerAdvice.handleNotFoundException(ex);
    
    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ChatErrorResponseDto body = (ChatErrorResponseDto) response.getBody();
    assertEquals(HttpStatus.NOT_FOUND, body.getStatusCode());
    assertEquals("Resource not found", body.getMessage());
  }

  @Test
  void testHandleRuntimeException() {
    // Arrange
    RuntimeException ex = new RuntimeException("Unexpected error");
    
    // Act
    var response = globalControllerAdvice.handleRuntimeException();
    
    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Server error. Try later", response.getBody());
  }
  
  @Test
  void testHandleDataIntegrityViolationException() throws Exception {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    SQLException rootCause = mock(SQLException.class);
    when(ex.getRootCause()).thenReturn(rootCause);
    when(rootCause.getLocalizedMessage()).thenReturn("Key (name)=(testUser) already exists");
    
    // Act
    var response = globalControllerAdvice.handleUniqueConstraintViolationException(ex);
    
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ChatErrorResponseDto body = response.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, body.getStatusCode());
    assertEquals("'testUser' is already in use", body.getMessage());
  }

  @Test
  void testHandleResourceInUseException() {
    // Arrange
    ResourceInUseException ex = new ResourceInUseException("Resource is currently in use");
    
    // Act
    var response = globalControllerAdvice.handleResourceInUseException(ex);
    
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ChatErrorResponseDto body = response.getBody();
    assertEquals(HttpStatus.BAD_REQUEST, body.getStatusCode());
    assertEquals("Resource is currently in use", body.getMessage());
  }
}