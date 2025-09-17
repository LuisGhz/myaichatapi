package dev.luisghtz.myaichat.chat.entities;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.prompts.entities.CustomPrompt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class Chat {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(nullable = true)
  private String title;
  private Date createdAt;
  private String model;
  @JsonIgnore
  @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
  private List<AppMessage> messages;
  @ManyToOne(fetch = FetchType.LAZY)
  private CustomPrompt customPrompt;
  private Short maxOutputTokens;
  private Boolean fav;
  @Column(nullable = false, columnDefinition = "boolean default false")
  private Boolean isWebSearchMode;
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
}
