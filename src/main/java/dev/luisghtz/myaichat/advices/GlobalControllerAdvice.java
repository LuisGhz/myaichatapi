package dev.luisghtz.myaichat.advices;

import java.util.stream.Collectors;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import dev.luisghtz.myaichat.chat.dtos.ChatErrorResponseDto;
import dev.luisghtz.myaichat.exceptions.AppMethodArgumentNotValidException;
import dev.luisghtz.myaichat.exceptions.AppNotFoundException;
import dev.luisghtz.myaichat.exceptions.ImageNotValidException;

@ControllerAdvice
public class GlobalControllerAdvice {
  @ExceptionHandler({ MethodArgumentNotValidException.class })
  public ResponseEntity<ChatErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

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

  @ExceptionHandler(ImageNotValidException.class)
  public ResponseEntity<ChatErrorResponseDto> handleImageNotValidException(ImageNotValidException ex) {
    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message(ex.getMessage())
        .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AppNotFoundException.class)
  public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.NOT_FOUND)
        .message(ex.getMessage())
        .build();

    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Object> handleRuntimeException() {
    return ResponseEntity.internalServerError().body("Server error. Try later");
  }
}
