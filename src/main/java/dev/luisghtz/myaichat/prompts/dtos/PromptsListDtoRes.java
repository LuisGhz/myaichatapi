package dev.luisghtz.myaichat.prompts.dtos;

import java.util.List;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptsListDtoRes {
  List<PromptSummaryResDto> prompts;
}
