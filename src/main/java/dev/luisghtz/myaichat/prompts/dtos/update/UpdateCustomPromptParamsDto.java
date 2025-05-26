package dev.luisghtz.myaichat.prompts.dtos.update;

import org.hibernate.validator.constraints.Length;

import dev.luisghtz.myaichat.validators.NoBlankSpace;
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
  @NoBlankSpace(message = "Param name is required")
  @Length(max = 15, message = "Param name must be 15 characters or less")
  private String name;
  @NoBlankSpace(message = "Param value is required")
  @Length(max = 100, message = "Param value must be 100 characters or less")
  private String value;
}
