package dev.luisghtz.myaichat.image.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {
  @Value("${files.max-mb-size}")
  private short maxMBSize;

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    if (file == null)
      return true; // Null files are considered valid (optional field)

    if (file.isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("File is empty").addConstraintViolation();
      return false;
    }

    long fileSize = file.getSize();
    String contentType = file.getContentType();
    if (fileSize > maxMBSize * 1024 * 1024) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("File size exceeds the limit of " + maxMBSize + "MB")
          .addConstraintViolation();
      return false;
    }

    if (!contentType.startsWith("image/")) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("File is not an image").addConstraintViolation();
      return false;
    }
    
    return true;
  }

}
