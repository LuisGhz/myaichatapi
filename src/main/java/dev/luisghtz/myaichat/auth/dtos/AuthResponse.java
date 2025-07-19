package dev.luisghtz.myaichat.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UserDto user;
    
    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }
}
