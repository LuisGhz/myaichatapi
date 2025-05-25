package dev.luisghtz.myaichat.prompts.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptDtoReq;
import dev.luisghtz.myaichat.prompts.dtos.CreateCustomPromptParamsDto;

public class ParamsInContentValidator implements ConstraintValidator<ParamsInContent, CreateCustomPromptDtoReq> {
  private String message;

  @Override
  public void initialize(ParamsInContent constraintAnnotation) {
    this.message = constraintAnnotation.message();
  }

  @Override
  public boolean isValid(CreateCustomPromptDtoReq value, ConstraintValidatorContext context) {
    if (value.getParams() == null || value.getParams().isEmpty())
      return true;
    for (CreateCustomPromptParamsDto param : value.getParams()) {
      // Empty param name is validated by @NotBlank
      if (!param.getName().trim().isEmpty() &&
          !value.getContent().contains("{" + param.getName() + "}")) {
        context.disableDefaultConstraintViolation();

        // Add custom error message
        context.buildConstraintViolationWithTemplate(
            message)
            .addConstraintViolation();
        return false;
      }
    }

    return true;
  }

}
