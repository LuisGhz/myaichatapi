package dev.luisghtz.myaichat.prompts.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;

public interface CustomRepository extends JpaRepository<CustomPrompt, UUID> {
  @Query("SELECT new dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto(p.id, p.name) FROM CustomPrompt p WHERE p.user.id = :userId")
  List<PromptSummaryResDto> findAllSummaryByUserId(@Param("userId") UUID userId);
  
  @Query("SELECT new dev.luisghtz.myaichat.prompts.dtos.PromptSummaryResDto(p.id, p.name) FROM CustomPrompt p")
  List<PromptSummaryResDto> findAllSummary();
  
  Optional<CustomPrompt> findByIdAndUserId(UUID id, UUID userId);
  
  List<CustomPrompt> findByUserId(UUID userId);
  
  @Query("SELECT COUNT(c) > 0 FROM Chat c WHERE c.customPrompt.id = :promptId")
  boolean hasAssociatedChats(@Param("promptId") UUID promptId);
}
