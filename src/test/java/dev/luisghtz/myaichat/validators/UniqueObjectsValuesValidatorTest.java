package dev.luisghtz.myaichat.validators;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniqueObjectsValuesValidatorTest {

  private UniqueObjectsValuesValidator validator;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new UniqueObjectsValuesValidator();
    context = Mockito.mock(ConstraintValidatorContext.class);
  }

  @Test
  void testNullList() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("name");
    validator.initialize(annotation);

    // Assert
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void testEmptyList() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("name");
    validator.initialize(annotation);

    // Assert
    assertTrue(validator.isValid(new ArrayList<>(), context));
  }

  @Test
  void testUniqueValues() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("name");
    validator.initialize(annotation);

    List<TestObject> objects = Arrays.asList(
        new TestObject("value1"),
        new TestObject("value2"),
        new TestObject("value3"));

    // Assert
    assertTrue(validator.isValid(objects, context));
  }

  @Test
  void testDuplicateValues() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("name");
    validator.initialize(annotation);

    List<TestObject> objects = Arrays.asList(
        new TestObject("value1"),
        new TestObject("value2"),
        new TestObject("value1") // Duplicate
    );

    // Assert
    assertFalse(validator.isValid(objects, context));
  }

  @Test
  void testDuplicateValuesWithDifferentCase() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("name");
    validator.initialize(annotation);

    List<TestObject> objects = Arrays.asList(
        new TestObject("Value"),
        new TestObject("value") // Same as "Value" when lowercase
    );

    // Assert
    assertFalse(validator.isValid(objects, context));
  }

  @Test
  void testNonExistentField() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("nonExistentField");
    validator.initialize(annotation);

    List<TestObject> objects = Arrays.asList(new TestObject("value"));

    // Assert
    assertThrows(RuntimeException.class, () -> validator.isValid(objects, context));
  }

  @Test
  void testNonStringField() {
    // Set up
    UniqueObjectsValues annotation = Mockito.mock(UniqueObjectsValues.class);
    Mockito.when(annotation.fieldName()).thenReturn("count");
    validator.initialize(annotation);

    List<ObjectWithNonStringField> objects = Arrays.asList(new ObjectWithNonStringField(1));

    // Assert
    assertThrows(RuntimeException.class, () -> validator.isValid(objects, context));
  }

  // Test helper classes
  static class TestObject {
    private String name;

    public TestObject(String name) {
      this.name = name;
    }
  }

  static class ObjectWithNonStringField {
    private int count;

    public ObjectWithNonStringField(int count) {
      this.count = count;
    }
  }
}