package dev.luisghtz.myaichat.validators;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueObjectsValuesValidator implements ConstraintValidator<UniqueObjectsValues, List<?>> {
  private String fieldName;

  @Override
  public void initialize(UniqueObjectsValues constraintAnnotation) {
    this.fieldName = constraintAnnotation.fieldName();
  }

  @Override
  public boolean isValid(List<?> value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true; // or false based on your validation logic
    }
    Set<String> seenValues = new HashSet<>();
    for (Object obj : value) {
      String fieldValue = getFieldValue(obj);
      if (seenValues.contains(fieldValue)) {
        return false; // Duplicate found
      }
      seenValues.add(fieldValue.toLowerCase());
    }

    return true;
  }

  private String getFieldValue(Object obj) {
    try {
      Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return (String) field.get(obj);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Error retrieving field value", e);
    }
  }

}
