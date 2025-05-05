package dev.luisghtz.myaichat.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = AllowedStringValuesValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedStringValues {
  String message() default "Invalid values";

  Class<?> values();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
