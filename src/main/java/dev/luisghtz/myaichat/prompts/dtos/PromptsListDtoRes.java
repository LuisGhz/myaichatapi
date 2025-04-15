package dev.luisghtz.myaichat.prompts.dtos;

import java.util.List;

import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptsListDtoRes {
  List<CustomPrompt> prompts;
}
