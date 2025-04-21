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
  @NoBlankSpace(message = "Name cannot be blank")
  @Length(max = 15, message = "Name cannot exceed 15 characters")
  private String name;
  @NoBlankSpace(message = "Value cannot be blank")
  @Length(max = 100, message = "Value cannot exceed 100 characters")
  private String value;
}
