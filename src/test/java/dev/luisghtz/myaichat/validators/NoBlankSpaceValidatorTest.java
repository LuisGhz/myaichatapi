package dev.luisghtz.myaichat.validators;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoBlankSpaceValidatorTest {

  private NoBlankSpaceValidator validator;
  
  @BeforeEach
  void setUp() {
    validator = new NoBlankSpaceValidator();
  }
  
  @Test
  void whenValueIsNull_thenReturnsTrue() {
    assertTrue(validator.isValid(null, null));
  }
  
  @Test
  void whenValueIsEmpty_thenReturnsFalse() {
    assertFalse(validator.isValid("", null));
  }
  
  @Test
  void whenValueContainsOnlySpaces_thenReturnsFalse() {
    assertFalse(validator.isValid("   ", null));
  }
  
  @Test
  void whenValueContainsValidContent_thenReturnsTrue() {
    assertTrue(validator.isValid("valid", null));
  }
  
  @Test
  void whenValueContainsValidContentWithSpaces_thenReturnsTrue() {
    assertTrue(validator.isValid("valid content", null));
  }
  
  @Test
  void whenValueContainsSpacesAtBothEnds_thenReturnsTrue() {
    assertTrue(validator.isValid("  valid content  ", null));
  }
}