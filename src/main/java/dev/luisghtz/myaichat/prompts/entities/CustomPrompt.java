package dev.luisghtz.myaichat.prompts.entities;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.chat.entities.Chat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
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
  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;
  @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PromptMessage> messages;
  @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PromptParam> params;
  @OneToMany(mappedBy = "customPrompt", fetch = FetchType.LAZY)
  @JsonIgnore
  private List<Chat> chats;
}
