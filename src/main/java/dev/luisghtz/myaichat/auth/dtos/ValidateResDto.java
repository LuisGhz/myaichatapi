package dev.luisghtz.myaichat.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateResDto {
    private boolean valid;
    private UserDto user;
    private String message;
    
    // Convenience constructors for common scenarios
    public static ValidateResDto success(UserDto user) {
        return ValidateResDto.builder()
                .valid(true)
                .user(user)
                .build();
    }
    
    public static ValidateResDto failure(String message) {
        return ValidateResDto.builder()
                .valid(false)
                .message(message)
                .build();
    }
}
