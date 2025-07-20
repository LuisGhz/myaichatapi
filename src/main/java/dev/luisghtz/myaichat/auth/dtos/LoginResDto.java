package dev.luisghtz.myaichat.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResDto {
  private String message;
  private String githubLoginUrl;  
}
