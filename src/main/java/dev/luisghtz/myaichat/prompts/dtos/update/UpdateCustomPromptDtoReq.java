package dev.luisghtz.myaichat.prompts.dtos.update;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import dev.luisghtz.myaichat.validators.NoBlankSpace;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomPromptDtoReq {
  @NoBlankSpace(message = "Name is required")
  @Length(max = 30, message = "Content must be 30 characters or less")
  private String name;
  @NoBlankSpace(message = "Content is required")
  @Length(max = 10_000, message = "Content must be 10,000 characters or less")
  private String content;
  @Valid
  private List<UpdateCustomPromptMessagesDto> messages;
}
