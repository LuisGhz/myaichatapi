package dev.luisghtz.myaichat.validators;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedStringValuesValidator implements ConstraintValidator<AllowedStringValues, String> {

  private List<String> values;

  @Override
  public void initialize(AllowedStringValues constraintAnnotation) {
    values = Arrays.asList(constraintAnnotation.values());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return values.contains(value);
  }

}
