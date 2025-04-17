package dev.luisghtz.myaichat.prompts.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.luisghtz.myaichat.prompts.entities.PromptParam;

public interface PromptParamRepository extends JpaRepository<PromptParam, UUID> {
  void deleteByIdAndPromptId(UUID id, UUID promptId);
}
