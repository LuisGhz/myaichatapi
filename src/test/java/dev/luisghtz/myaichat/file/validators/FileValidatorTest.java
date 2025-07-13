package dev.luisghtz.myaichat.file.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

class FileValidatorTest {

  private FileValidator validator;

  @Mock
  private MultipartFile mockFile;

  @Mock
  private ConstraintValidatorContext mockContext;

  @Mock
  private ConstraintViolationBuilder mockViolationBuilder;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new FileValidator();
    ReflectionTestUtils.setField(validator, "maxMBSize", (short) 5);
  }

  @Test
  void testIsValid_NullFile_ReturnsTrue() {
    boolean result = validator.isValid(null, mockContext);
    assertTrue(result);
  }

  @Test
  void testIsValid_EmptyFile_ReturnsFalse() {
    when(mockFile.isEmpty()).thenReturn(true);
    when(mockContext.buildConstraintViolationWithTemplate("File is empty")).thenReturn(mockViolationBuilder);
    when(mockViolationBuilder.addConstraintViolation()).thenReturn(mockContext);

    boolean result = validator.isValid(mockFile, mockContext);

    assertFalse(result);
    verify(mockContext).disableDefaultConstraintViolation();
    verify(mockContext).buildConstraintViolationWithTemplate("File is empty");
    verify(mockViolationBuilder).addConstraintViolation();
  }

  @Test
  void testIsValid_FileSizeExceedsLimit_ReturnsFalse() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(6L * 1024 * 1024); // 6MB
    when(mockContext.buildConstraintViolationWithTemplate("File size exceeds the limit of 5MB"))
        .thenReturn(mockViolationBuilder);
    when(mockViolationBuilder.addConstraintViolation()).thenReturn(mockContext);

    boolean result = validator.isValid(mockFile, mockContext);

    assertFalse(result);
    verify(mockContext).disableDefaultConstraintViolation();
    verify(mockContext).buildConstraintViolationWithTemplate("File size exceeds the limit of 5MB");
    verify(mockViolationBuilder).addConstraintViolation();
  }

  @Test
  void testIsValid_InvalidContentType_ReturnsFalse() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L); // 1KB
    when(mockFile.getContentType()).thenReturn("application/pdf");
    when(mockContext.buildConstraintViolationWithTemplate("File type not supported")).thenReturn(mockViolationBuilder);
    when(mockViolationBuilder.addConstraintViolation()).thenReturn(mockContext);

    boolean result = validator.isValid(mockFile, mockContext);

    assertFalse(result);
    verify(mockContext).disableDefaultConstraintViolation();
    verify(mockContext).buildConstraintViolationWithTemplate("File type not supported");
    verify(mockViolationBuilder).addConstraintViolation();
  }

  @Test
  void testIsValid_ValidJpegFile_ReturnsTrue() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L); // 1KB
    when(mockFile.getContentType()).thenReturn("image/jpeg");

    boolean result = validator.isValid(mockFile, mockContext);

    assertTrue(result);
    verify(mockContext, never()).disableDefaultConstraintViolation();
  }

  @Test
  void testIsValid_ValidPngFile_ReturnsTrue() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L); // 1KB
    when(mockFile.getContentType()).thenReturn("image/png");

    boolean result = validator.isValid(mockFile, mockContext);

    assertTrue(result);
    verify(mockContext, never()).disableDefaultConstraintViolation();
  }

  @Test
  void testIsValid_ValidGifFile_ReturnsTrue() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L); // 1KB
    when(mockFile.getContentType()).thenReturn("image/gif");

    boolean result = validator.isValid(mockFile, mockContext);

    assertTrue(result);
    verify(mockContext, never()).disableDefaultConstraintViolation();
  }

  @Test
  void testIsValid_ValidJpgFile_ReturnsTrue() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L); // 1KB
    when(mockFile.getContentType()).thenReturn("image/jpg");

    boolean result = validator.isValid(mockFile, mockContext);

    assertTrue(result);
    verify(mockContext, never()).disableDefaultConstraintViolation();
  }

  @Test
  void testIsValid_FileSizeAtLimit_ReturnsTrue() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(5L * 1024 * 1024); // Exactly 5MB
    when(mockFile.getContentType()).thenReturn("image/jpeg");

    boolean result = validator.isValid(mockFile, mockContext);

    assertTrue(result);
    verify(mockContext, never()).disableDefaultConstraintViolation();
  }
}
