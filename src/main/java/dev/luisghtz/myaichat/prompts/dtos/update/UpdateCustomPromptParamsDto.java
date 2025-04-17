package dev.luisghtz.myaichat.prompts.dtos.update;

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
  private String name;
  @NoBlankSpace(message = "Value cannot be blank")
  private String value;
}
