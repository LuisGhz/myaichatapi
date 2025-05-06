package dev.luisghtz.myaichat.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Helper class to provide the list of allowed strings via a static method
class TestValuesProvider {
    private static List<String> currentTestValues = Collections.emptyList();

    public static void setAllowedValues(List<String> values) {
        currentTestValues = values;
    }

    // This is the method the validator will call via reflection
    public static List<String> allStringValues() {
        return currentTestValues;
    }
}

// Helper class for testing missing method scenario
class ProviderWithoutMethod {}

// Helper class for testing non-static method scenario
class ProviderWithNonStaticMethod {
    // Non-static method
    public List<String> allStringValues() {
        return Arrays.asList("X");
    }
}

// Helper class for testing wrong return type scenario
class ProviderWithWrongReturnType {
    // Returns String[] instead of List<String>
    public static String[] allStringValues() {
        return new String[]{"X"};
    }
}

// Helper class for testing scenario where the provider method throws an exception
class ProviderMethodThrowsException {
    public static List<String> allStringValues() {
        throw new IllegalArgumentException("Test internal exception from provider");
    }
}

// Mock implementation of the AllowedStringValues annotation
class MockAllowedStringValuesAnnotation implements AllowedStringValues {
    private final Class<?> providerClass;

    MockAllowedStringValuesAnnotation(Class<?> providerClass) {
        this.providerClass = providerClass;
    }

    @Override
    public Class<?> values() { // Returns the class that provides the allowed values
        return providerClass;
    }

    @Override
    public String message() {
        return "Invalid value. Must be one of provided values.";
    }

    @Override
    public Class<?>[] groups() {
        return new Class<?>[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends jakarta.validation.Payload>[] payload() {
        return (Class<? extends jakarta.validation.Payload>[]) new Class[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return AllowedStringValues.class;
    }
}

public class AllowedStringValuesValidatorTest {

    private AllowedStringValuesValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AllowedStringValuesValidator();
        context = null; // ConstraintValidatorContext is not used by the current isValid logic
        // Reset the provider for each test to ensure test isolation
        TestValuesProvider.setAllowedValues(Collections.emptyList());
    }

    private void initializeValidatorWithValues(List<String> values) {
        TestValuesProvider.setAllowedValues(values);
        AllowedStringValues annotation = new MockAllowedStringValuesAnnotation(TestValuesProvider.class);
        validator.initialize(annotation);
    }

    @Test
    void testIsValid_withAllowedValue_returnsTrue() {
        initializeValidatorWithValues(Arrays.asList("A", "B", "C"));
        assertTrue(validator.isValid("A", context), "Value 'A' should be valid.");
        assertTrue(validator.isValid("B", context), "Value 'B' should be valid.");
        assertTrue(validator.isValid("C", context), "Value 'C' should be valid.");
    }

    @Test
    void testIsValid_withDisallowedValue_returnsFalse() {
        initializeValidatorWithValues(Arrays.asList("A", "B", "C"));
        assertFalse(validator.isValid("D", context), "Value 'D' should be invalid.");
        assertFalse(validator.isValid("E", context), "Value 'E' should be invalid.");
    }

    @Test
    void testIsValid_withNullValue_returnsTrue() {
        initializeValidatorWithValues(Arrays.asList("A", "B", "C"));
        assertTrue(validator.isValid(null, context), "Null value should be considered valid by default.");
    }

    @Test
    void testIsValid_withEmptyAllowedValues_returnsFalseForAnyNonNullValue() {
        initializeValidatorWithValues(Collections.emptyList());
        assertFalse(validator.isValid("A", context), "Value 'A' should be invalid when allowed list is empty.");
        assertFalse(validator.isValid("", context), "Empty string should be invalid when allowed list is empty.");
    }

    @Test
    void testIsValid_withEmptyStringInAllowedValues_andValueIsEmptyString_returnsTrue() {
        initializeValidatorWithValues(Arrays.asList("A", "", "C"));
        assertTrue(validator.isValid("", context), "Empty string should be valid if it's in the allowed list.");
    }

    @Test
    void testIsValid_withEmptyStringInAllowedValues_andValueIsNonEmptyDisallowed_returnsFalse() {
        initializeValidatorWithValues(Arrays.asList("A", "", "C"));
        assertFalse(validator.isValid("B", context), "Value 'B' should be invalid.");
    }

    // Tests for initialization phase errors

    @Test
    void testInitialize_whenProviderClassMissingMethod_throwsRuntimeException() {
        AllowedStringValues annotation = new MockAllowedStringValuesAnnotation(ProviderWithoutMethod.class);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            validator.initialize(annotation);
        }, "Should throw RuntimeException if provider class is missing 'allStringValues' method.");
        assertTrue(exception.getMessage().contains("Error initializing AllowedStringValuesValidator"));
    }

    @Test
    void testInitialize_whenMethodNotStatic_throwsRuntimeException() {
        AllowedStringValues annotation = new MockAllowedStringValuesAnnotation(ProviderWithNonStaticMethod.class);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            validator.initialize(annotation);
        }, "Should throw RuntimeException if 'allStringValues' method is not static.");
        assertTrue(exception.getMessage().contains("Error initializing AllowedStringValuesValidator"));
        // Cause is likely InvocationTargetException -> NullPointerException
    }

    @Test
    void testInitialize_whenMethodHasWrongReturnType_throwsRuntimeException() {
        AllowedStringValues annotation = new MockAllowedStringValuesAnnotation(ProviderWithWrongReturnType.class);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            validator.initialize(annotation);
        }, "Should throw RuntimeException if 'allStringValues' method has wrong return type.");
        assertTrue(exception.getMessage().contains("Error initializing AllowedStringValuesValidator"));
        // Cause is likely ClassCastException
    }

    @Test
    void testInitialize_whenMethodThrowsException_throwsRuntimeException() {
        AllowedStringValues annotation = new MockAllowedStringValuesAnnotation(ProviderMethodThrowsException.class);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            validator.initialize(annotation);
        }, "Should throw RuntimeException if 'allStringValues' method throws an exception.");
        assertTrue(exception.getMessage().contains("Error initializing AllowedStringValuesValidator"));
        // Cause is likely InvocationTargetException -> (original exception)
    }
}
