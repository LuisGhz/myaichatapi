package dev.luisghtz.myaichat.exceptions;

public class AppNotFoundException extends RuntimeException {
  public AppNotFoundException(String message) {
    super(message);
  }

  public AppNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppNotFoundException(Throwable cause) {
    super(cause);
  }

}
