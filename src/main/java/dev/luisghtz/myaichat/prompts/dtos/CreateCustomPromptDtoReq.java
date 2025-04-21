package dev.luisghtz.myaichat.prompts.dtos;

import java.util.List;

import dev.luisghtz.myaichat.validators.UniqueObjectsValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomPromptDtoReq {
  @NotBlank
  @Max(value = 15, message = "Name cannot exceed 15 characters")
  private String name;
  @NotBlank
  @Max(value = 10_000, message = "Content cannot exceed 10,000 characters")
  private String content;
  @Valid
  @UniqueObjectsValues(fieldName = "name", message = "Params names must be unique")
  private List<CreateCustomPromptParamsDto> params;
  @Valid
  private List<CreateCustomPromptMessagesDto> messages;
}
