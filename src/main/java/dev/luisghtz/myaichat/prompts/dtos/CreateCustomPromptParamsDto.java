package dev.luisghtz.myaichat.prompts.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomPromptParamsDto {
  @NotBlank
  @Max(value = 15, message = "Name cannot exceed 15 characters")
  private String name;
  @NotBlank
  @Max(value = 100, message = "Value cannot exceed 100 characters")
  private String value;
}
