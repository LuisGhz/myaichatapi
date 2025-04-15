package dev.luisghtz.myaichat.prompts.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomPromptDtoReq {
  @NotBlank
  private String name;
  @NotBlank
  private String model;
  @NotBlank
  private String systemMessage;
  private List<String> params;
}
