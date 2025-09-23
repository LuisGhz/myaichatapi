package dev.luisghtz.myaichat.chat.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameChatTitleDto {
  @NotBlank
  @Length(min = 1, max = 50)
  private String name;
}
