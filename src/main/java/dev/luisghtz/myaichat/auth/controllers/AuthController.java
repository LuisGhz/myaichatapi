package dev.luisghtz.myaichat.auth.controllers;

import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.AuthService;
import dev.luisghtz.myaichat.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Please initiate OAuth2 login");
        response.put("github_login_url", "/oauth2/authorization/github");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Invalid token format"));
            }
            
            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);
            
            if (isValid) {
                User user = authService.getUserFromToken(token);
                if (user != null) {
                    UserDto userDto = userService.convertToDto(user);
                    return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "user", userDto
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of("valid", false, "message", "Invalid token"));
            
        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity.ok(Map.of("valid", false, "message", "Token validation failed"));
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().build();
            }
            
            String token = authHeader.substring(7);
            User user = authService.getUserFromToken(token);
            
            if (user != null) {
                UserDto userDto = userService.convertToDto(user);
                return ResponseEntity.ok(userDto);
            }
            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Authentication service is running");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
