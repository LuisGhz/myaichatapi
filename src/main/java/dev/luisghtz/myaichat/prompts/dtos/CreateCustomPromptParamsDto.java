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
  @Length(max = 15, message = "Name cannot exceed 15 characters")
  private String name;
  @NotBlank
  @Length(max = 100, message = "Value cannot exceed 100 characters")
  private String value;
}
