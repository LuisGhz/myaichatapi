package dev.luisghtz.myaichat.prompts.dtos.update;

import dev.luisghtz.myaichat.validators.NoBlankSpace;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomPromptParamsDto {
  private String id;
  @NoBlankSpace(message = "Name cannot be blank")
  @Max(value = 15, message = "Name cannot exceed 15 characters")
  private String name;
  @NoBlankSpace(message = "Value cannot be blank")
  @Max(value = 100, message = "Name cannot exceed 100 characters")
  private String value;
}
