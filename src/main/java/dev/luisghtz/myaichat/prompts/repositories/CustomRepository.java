package dev.luisghtz.myaichat.prompts.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;

public interface CustomRepository extends JpaRepository<CustomPrompt, UUID> {
  @Query("SELECT new dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto(p.id, p.name) FROM CustomPrompt p")
  List<PromptSummaryResDto> findAllSummary();
}
