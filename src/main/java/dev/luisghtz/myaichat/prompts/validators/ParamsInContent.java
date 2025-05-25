package dev.luisghtz.myaichat.prompts.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.api.client.json.webtoken.JsonWebToken.Payload;

import jakarta.validation.Constraint;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ParamsInContentValidator.class)
public @interface ParamsInContent {
  String message() default "All parameters must be in the content.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
