package dev.luisghtz.myaichat.audio.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptionReqDto {
  @NotNull(message = "Audio file is required")
  private MultipartFile audio;
}
