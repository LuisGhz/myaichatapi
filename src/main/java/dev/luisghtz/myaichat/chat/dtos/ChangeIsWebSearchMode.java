package dev.luisghtz.myaichat.chat.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeIsWebSearchMode {
  @NotNull
  private boolean isWebSearchMode;
}
