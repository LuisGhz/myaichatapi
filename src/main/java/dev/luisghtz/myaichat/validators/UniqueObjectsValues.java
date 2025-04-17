package dev.luisghtz.myaichat.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = UniqueObjectsValuesValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueObjectsValues {
  String message() default "Params must be unique";

  String fieldName();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
