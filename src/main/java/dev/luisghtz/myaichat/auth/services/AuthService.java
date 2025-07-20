package dev.luisghtz.myaichat.auth.services;

import dev.luisghtz.myaichat.auth.dtos.AuthResponse;
import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    public AuthResponse authenticateWithGitHub(Map<String, Object> githubUser) {
        log.info("Authenticating user with GitHub: {}", githubUser != null ? githubUser.get("login") : null);
        
        // Create or update user
        User user = userService.createOrUpdateUserFromGitHub(githubUser);
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        
        // Convert to DTO
        UserDto userDto = userService.convertToDto(user);
        
        log.info("Successfully authenticated user: {}", user.getUsername());
        return new AuthResponse(token, userDto);
    }
    
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
    
    public User getUserFromToken(String token) {
        UUID userId = jwtService.getUserIdFromToken(token);
        String userIdString = userId != null ? userId.toString() : "null";
        return userService.findById(userIdString).orElse(null);
    }
}
