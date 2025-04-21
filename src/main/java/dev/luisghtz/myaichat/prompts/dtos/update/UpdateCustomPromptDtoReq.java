package dev.luisghtz.myaichat.prompts.dtos.update;

import java.util.List;

import dev.luisghtz.myaichat.validators.NoBlankSpace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomPromptDtoReq {
  @NoBlankSpace(message = "Name cannot be blank")
  @Max(value = 15, message = "Name cannot exceed 15 characters")
  private String name;
  @Max(value = 10_000, message = "Name cannot exceed 10,000 characters")
  @NoBlankSpace(message = "Content cannot be blank")
  private String content;
  @Valid
  private List<UpdateCustomPromptParamsDto> params;
  @Valid
  private List<UpdateCustomPromptMessagesDto> messages;
}
