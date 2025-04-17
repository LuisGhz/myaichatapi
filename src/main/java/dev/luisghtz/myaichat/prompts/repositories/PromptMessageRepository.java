package dev.luisghtz.myaichat.prompts.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.luisghtz.myaichat.prompts.entities.PromptMessage;

public interface PromptMessageRepository extends JpaRepository<PromptMessage, UUID> {
  void deleteByIdAndPromptId(UUID id, UUID promptId);
}
