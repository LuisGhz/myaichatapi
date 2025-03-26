package dev.luisghtz.myaichat.chat.dtos;

import java.util.List;

import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatErrorResponseDto {
  private HttpStatusCode statusCode;
  private String message;
  private List<String> messages;
}
