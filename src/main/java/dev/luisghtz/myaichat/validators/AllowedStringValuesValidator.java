package dev.luisghtz.myaichat.validators;

import java.lang.reflect.Method;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AllowedStringValuesValidator implements ConstraintValidator<AllowedStringValues, String> {

  private List<String> values;
  private final String methodName = "allStringValues";

  @Override
  public void initialize(AllowedStringValues constraintAnnotation) {
    Class<?> clazz = constraintAnnotation.values();
    try {
      Method method = clazz.getMethod(methodName);
      values = (List<String>) method.invoke(null);
    } catch (Exception e) {
      log.error("Error initializing AllowedStringValuesValidator");
      throw new RuntimeException("Error initializing AllowedStringValuesValidator");
    }
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    log.info(value);
    log.info(values.toString());
    if (value == null)
      return true;
    return values.contains(value);
  }

}
