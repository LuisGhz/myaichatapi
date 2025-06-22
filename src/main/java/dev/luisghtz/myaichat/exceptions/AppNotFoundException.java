package dev.luisghtz.myaichat.exceptions;

public class AppNotFoundException extends RuntimeException {
  public AppNotFoundException(String message) {
    super(message);
  }

}
