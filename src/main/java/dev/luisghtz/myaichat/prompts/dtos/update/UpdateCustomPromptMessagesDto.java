package dev.luisghtz.myaichat.prompts.dtos.update;

import java.util.UUID;

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
  @AllowedStringValues(values = {"User", "Assistant"}, message = "Role should be \"User\" or \"Assistant\"")
  private String role;
  @NoBlankSpace(message = "Content cannot be blank")
  private String content;
}
