package dev.luisghtz.myaichat.advices;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.chat.dtos.ChatErrorResponseDto;
import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.exceptions.ResourceInUseException;

@ControllerAdvice
public class GlobalControllerAdvice {
  @ExceptionHandler({ MethodArgumentNotValidException.class })
  public ResponseEntity<ChatErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
    StringBuilder messageBuilder = new StringBuilder();

    // Handle field errors
    if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
      String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
          .map(error -> error.getField() + ": " + error.getDefaultMessage())
          .collect(Collectors.joining(", "));
      messageBuilder.append(fieldErrors);
    }

    // Handle global/class-level errors
    if (!ex.getBindingResult().getGlobalErrors().isEmpty()) {
      String globalErrors = ex.getBindingResult().getGlobalErrors().stream()
          .map(error -> error.getDefaultMessage())
          .collect(Collectors.joining(", "));

      if (messageBuilder.length() > 0) {
        messageBuilder.append(", ");
      }
      messageBuilder.append(globalErrors);
    }

    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message(messageBuilder.toString())
        .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({ IllegalArgumentException.class })
  public ResponseEntity<ChatErrorResponseDto> handleValidationException(IllegalArgumentException ex) {
    String message = ex.getMessage();

    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message("Validation error: " + message)
        .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({ AppMethodArgumentNotValidException.class })
  public ResponseEntity<ChatErrorResponseDto> handleValidationException(AppMethodArgumentNotValidException ex) {
    String message = ex.getMessage();

    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message("Validation error: " + message)
        .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ChatErrorResponseDto> handleResponseStatusException(ResponseStatusException ex) {
    var statusCode = ex.getStatusCode();
    var message = ex.getMessage();

    var response = ChatErrorResponseDto.builder()
        .statusCode(statusCode)
        .message(message)
        .build();

    return new ResponseEntity<>(response, statusCode);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
    var message = ex.getMessage();
    return ResponseEntity.internalServerError().body("Server error: " + message);
  }

  public ResponseEntity<Object> handleRuntimeException() {
    return ResponseEntity.internalServerError().body("Server error. Try later");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ChatErrorResponseDto> handleUniqueConstraintViolationException(
      DataIntegrityViolationException ex) {
    String message = getErrorMessage(ex.getRootCause().getLocalizedMessage());
    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message(message)
        .build();
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceInUseException.class)
  public ResponseEntity<ChatErrorResponseDto> handleResourceInUseException(ResourceInUseException ex) {
    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message(ex.getMessage())
        .build();
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  private String getErrorMessage(String message) {
    if (message.contains("Key (name)=")) {
      // Extract name from the error message using regex
      Pattern pattern = Pattern.compile("Key \\(name\\)=\\((.+?)\\) already exists");
      Matcher matcher = pattern.matcher(message);
      if (matcher.find()) {
        return "'" + matcher.group(1) + "' is already in use";
      }
    }
    return message;
  }
}
