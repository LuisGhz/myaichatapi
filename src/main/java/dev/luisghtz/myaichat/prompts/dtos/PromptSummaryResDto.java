package dev.luisghtz.myaichat.prompts.dtos;

import java.util.UUID;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptSummaryResDto {
  private UUID id;
  private String name;
}
