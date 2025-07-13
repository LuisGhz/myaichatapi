package dev.luisghtz.myaichat.file.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.RetentionPolicy;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
@Documented
public @interface ValidFile {
  String message() default "Invalid file: must be a valid file.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
