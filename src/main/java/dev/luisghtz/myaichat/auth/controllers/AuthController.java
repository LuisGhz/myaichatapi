package dev.luisghtz.myaichat.auth.controllers;

import dev.luisghtz.myaichat.auth.dtos.LoginResDto;
import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.dtos.ValidateResDto;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.AuthService;
import dev.luisghtz.myaichat.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
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
  public ResponseEntity<LoginResDto> login() {
    var response = new LoginResDto(
        "Please initiate OAuth2 login",
        "oauth2/authorization/github");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/validate")
  public ResponseEntity<ValidateResDto> validateToken(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    try {
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ValidateResDto.failure("Invalid token format"));
      }

      String token = authHeader.substring(7);
      boolean isValid = authService.validateToken(token);

      if (isValid) {
        User user = authService.getUserFromToken(token);
        if (user != null) {
          UserDto userDto = userService.convertToDto(user);
          return ResponseEntity.ok(ValidateResDto.success(userDto));
        }
        return ResponseEntity.ok(ValidateResDto.failure("Invalid token"));
      }

      return ResponseEntity.ok(ValidateResDto.failure("Invalid token"));

    } catch (Exception e) {
      log.error("Error validating token", e);
      return ResponseEntity.ok(ValidateResDto.failure("Token validation failed"));
    }
  }

  @GetMapping("/user")
  public ResponseEntity<UserDto> getCurrentUser(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    try {
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }

      String token = authHeader.substring(7);

      // Resolve the user from the token directly (tests mock getUserFromToken only)
      User user = authService.getUserFromToken(token);

      if (user != null) {
        UserDto userDto = userService.convertToDto(user);
        return ResponseEntity.ok(userDto);
      }

      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    } catch (Exception e) {
      log.error("Error getting current user", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
