package dev.luisghtz.myaichat.exceptions;

public class AppMethodArgumentNotValidException extends RuntimeException {
  public AppMethodArgumentNotValidException(String message) {
    super(message);
  }

  public AppMethodArgumentNotValidException(String message, Throwable cause) {
    super(message, cause);
  }

}
