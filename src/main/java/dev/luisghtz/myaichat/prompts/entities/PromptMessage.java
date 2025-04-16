package dev.luisghtz.myaichat.prompts.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "prompt_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromptMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String role;
  private String content;
  @ManyToOne
  @JoinColumn(name = "custom_prompt_id")
  @JsonIgnore
  private CustomPrompt prompt;
}
