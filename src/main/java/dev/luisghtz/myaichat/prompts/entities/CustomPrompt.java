package dev.luisghtz.myaichat.prompts.entities;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import dev.luisghtz.myaichat.chat.entities.Chat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomPrompt {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(nullable = false, unique = true)
  private String name;
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;
  @Column(updatable = false)
  private Date createdAt;
  @Column(insertable = false)
  private Date updatedAt;
  @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PromptMessage> messages;
  @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PromptParam> params;
  @OneToMany(mappedBy = "customPrompt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Chat> chats;
}
