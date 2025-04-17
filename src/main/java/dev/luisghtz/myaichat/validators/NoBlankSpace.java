package dev.luisghtz.myaichat.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = NoBlankSpaceValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoBlankSpace {
  String message() default "Field must not contain blank spaces";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
