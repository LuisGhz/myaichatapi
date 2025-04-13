package dev.luisghtz.myaichat.advices;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import dev.luisghtz.myaichat.chat.dtos.ChatErrorResponseDto;
import dev.luisghtz.myaichat.exceptions.ImageNotValidException;

@ControllerAdvice
public class GlobalControllerAdvice {
  @ExceptionHandler(MethodArgumentNotValidException.class)
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

  @ExceptionHandler(ImageNotValidException.class)
  public ResponseEntity<ChatErrorResponseDto> handleImageNotValidException(ImageNotValidException ex) {
    var response = ChatErrorResponseDto.builder()
        .statusCode(HttpStatus.BAD_REQUEST)
        .message(ex.getMessage())
        .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
