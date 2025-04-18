package dev.luisghtz.myaichat.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.ConstraintValidatorContext;

class DummyAllowedStringValues implements AllowedStringValues {
  private final String[] vals;

  DummyAllowedStringValues(String[] vals) {
    this.vals = vals;
  }

  @Override
  public String[] values() {
    return vals;
  }

  @Override
  public Class<? extends java.lang.annotation.Annotation> annotationType() {
    return AllowedStringValues.class;
  }

  @Override
  public String message() {
    return "";
  }

  @Override
  public Class<?>[] groups() {
    return new Class[0];
  }

  @Override
  public Class<? extends jakarta.validation.Payload>[] payload() {
    return new Class[0];
  }
}

public class AllowedStringValuesValidatorTest {

  private AllowedStringValuesValidator validator;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new AllowedStringValuesValidator();
    context = null; // Not used in current implementation
  }

  @Test
  void testIsValid_withAllowedValue_returnsTrue() {
    validator.initialize(new DummyAllowedStringValues(new String[] { "A", "B", "C" }));
    assertTrue(validator.isValid("A", context));
    assertTrue(validator.isValid("B", context));
    assertTrue(validator.isValid("C", context));
  }

  @Test
  void testIsValid_withDisallowedValue_returnsFalse() {
    validator.initialize(new DummyAllowedStringValues(new String[] { "A", "B", "C" }));
    assertFalse(validator.isValid("D", context));
    assertFalse(validator.isValid("E", context));
  }

  @Test
  void testIsValid_withNullValue_returnsTrue() {
    validator.initialize(new DummyAllowedStringValues(new String[] { "A", "B", "C" }));
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void testIsValid_withEmptyAllowedValues_returnsFalseForAnyNonNull() {
    validator.initialize(new DummyAllowedStringValues(new String[] {}));
    assertFalse(validator.isValid("A", context));
    assertFalse(validator.isValid("", context));
  }

  @Test
  void testIsValid_withEmptyStringAllowed() {
    validator.initialize(new DummyAllowedStringValues(new String[] { "" }));
    assertTrue(validator.isValid("", context));
    assertFalse(validator.isValid("A", context));
  }
}
