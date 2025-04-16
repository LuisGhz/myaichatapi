package dev.luisghtz.myaichat.prompts.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomPromptParamsDto {
  @NotBlank
  private String name;
  @NotBlank
  private String value;
}
