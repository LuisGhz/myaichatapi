package dev.luisghtz.myaichat.chat.dtos;

import java.util.List;

import dev.luisghtz.myaichat.chat.models.AppMessageHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryChatDto {
  private List<AppMessageHistory> historyMessages;
  private String model;
}
