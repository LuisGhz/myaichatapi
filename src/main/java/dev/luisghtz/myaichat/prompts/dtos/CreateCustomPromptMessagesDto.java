package dev.luisghtz.myaichat.prompts.dtos;

import dev.luisghtz.myaichat.validators.AllowedStringValues;
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
  private String content;
}
