package dev.luisghtz.myaichat.prompts.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;

public interface CustomRepository extends JpaRepository<CustomPrompt, UUID> {
  
}
