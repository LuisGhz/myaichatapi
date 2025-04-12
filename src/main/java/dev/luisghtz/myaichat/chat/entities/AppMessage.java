package dev.luisghtz.myaichat.chat.entities;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String role;
  @Column(columnDefinition = "TEXT")
  private String content;
  @Column(updatable = false)
  private Date createdAt;
  @Column(nullable = true)
  private Integer promptTokens;
  @Column(nullable = true)
  private Integer completionTokens;
  @Column(nullable = true)
  private Integer totalTokens;
  @Column(nullable = true)
  private String imageUrl;
  @JsonIgnore
  @ManyToOne()
  private Chat chat;
}
