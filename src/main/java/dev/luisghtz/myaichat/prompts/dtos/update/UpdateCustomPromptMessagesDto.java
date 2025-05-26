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
  @NoBlankSpace(message = "Role is required")
  @AllowedStringValues(values = AppPrompsRequestsRoles.class, message = "Role should be \"User\" or \"Assistant\"")
  private String role;
  @NoBlankSpace(message = "Content is required")
  @Length(max = 10_000, message = "Message content must be 10,000 characters or less")
  private String content;
}
