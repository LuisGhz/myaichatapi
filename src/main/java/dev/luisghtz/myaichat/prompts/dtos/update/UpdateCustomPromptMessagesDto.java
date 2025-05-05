package dev.luisghtz.myaichat.prompts.dtos.update;

import java.util.UUID;

import org.hibernate.validator.constraints.Length;

import dev.luisghtz.myaichat.prompts.models.AppPrompsRequestsRoles;
import dev.luisghtz.myaichat.validators.AllowedStringValues;
import dev.luisghtz.myaichat.validators.NoBlankSpace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomPromptMessagesDto {
  private UUID id;
  @NoBlankSpace(message = "Role cannot be blank")
  @AllowedStringValues(values = AppPrompsRequestsRoles.class, message = "Role should be \"User\" or \"Assistant\"")
  private String role;
  @NoBlankSpace(message = "Content cannot be blank")
  @Length(max = 10_000, message = "Content cannot exceed 10,000 characters")
  private String content;
}
