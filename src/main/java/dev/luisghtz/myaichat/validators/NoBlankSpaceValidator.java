package dev.luisghtz.myaichat.validators;

import jakarta.validation.ConstraintValidator;

public class NoBlankSpaceValidator implements ConstraintValidator<NoBlankSpace, String> {
  @Override
  public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
    if (value == null)
      return true;
    return !value.trim().isEmpty();
  }
  
}
