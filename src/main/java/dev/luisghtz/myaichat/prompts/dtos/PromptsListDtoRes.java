package dev.luisghtz.myaichat.prompts.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptsListDtoRes {
  List<PromptSummaryResDto> prompts;
}
