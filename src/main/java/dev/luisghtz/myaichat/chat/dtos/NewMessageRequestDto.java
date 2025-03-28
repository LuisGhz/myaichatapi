package dev.luisghtz.myaichat.chat.dtos;

import java.util.UUID;

import groovy.transform.builder.Builder;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewMessageRequestDto {
  private UUID chatId = null;
  @NotEmpty
  private String prompt;
  private String model = null;
}
