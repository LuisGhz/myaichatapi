package dev.luisghtz.myaichat.prompts.dtos;

import dev.luisghtz.myaichat.validators.AllowedStringValues;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomPromptMessagesDto {
  @NotBlank
  @AllowedStringValues(values = {"User", "Assistant"}, message = "Role should be \"User\" or \"Assistant\"")
  private String role;
  @NotBlank
  @Max(value = 10_000, message = "Content cannot exceed 10,000 characters")
  private String content;
}
