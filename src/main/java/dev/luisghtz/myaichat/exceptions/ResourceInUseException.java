package dev.luisghtz.myaichat.exceptions;

public class ResourceInUseException extends RuntimeException {
  public ResourceInUseException(String message) {
    super(message);
  }
}
