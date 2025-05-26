package dev.luisghtz.myaichat.prompts.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomPromptParamsDto {
  @NotBlank
  @Length(max = 15, message = "Must be 15 characters or less")
  private String name;
  @NotBlank
  @Length(max = 100, message = "Must be 100 characters or less")
  private String value;
}
