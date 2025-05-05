package dev.luisghtz.myaichat.chat.dtos;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import dev.luisghtz.myaichat.ai.models.AppModels;
import dev.luisghtz.myaichat.image.validators.ValidImage;
import dev.luisghtz.myaichat.validators.AllowedStringValues;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewMessageRequestDto {

  private UUID chatId = null;
  @NotEmpty
  private String prompt;
  @AllowedStringValues(values = AppModels.class, message = "Invalid model: must be one of the allowed values.")
  private String model = null;
  @ValidImage(message = "Invalid image: must be a valid image file.")
  private MultipartFile image = null;
  private String promptId;
}
