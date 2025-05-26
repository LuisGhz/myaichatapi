package dev.luisghtz.myaichat.chat.dtos;

import java.util.UUID;

import org.hibernate.validator.constraints.Length;
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
  @Length(max = 8_000, message = "Message must be at most 8,000 characters long.")
  private String prompt;
  @AllowedStringValues(values = AppModels.class, message = "Invalid model: must be one of the allowed models.")
  private String model = null;
  @ValidImage(message = "File type not supported (Only: jpg, jpeg, png, gif) or file size exceeded (max 2 MB).")
  private MultipartFile image = null;
  private String promptId;
}
