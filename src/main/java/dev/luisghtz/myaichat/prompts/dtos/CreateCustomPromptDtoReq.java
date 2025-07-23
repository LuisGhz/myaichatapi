package dev.luisghtz.myaichat.prompts.dtos;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomPromptDtoReq {
  @NotBlank
  @Length(max = 30, message = "Must be 30 characters or less")
  private String name;
  @NotBlank
  @Length(max = 10_000, message = "Content must be 10,000 characters or less")
  private String content;
  @Valid
  private List<CreateCustomPromptMessagesDto> messages;
}
