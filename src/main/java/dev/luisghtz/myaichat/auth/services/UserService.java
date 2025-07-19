package dev.luisghtz.myaichat.auth.services;

import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.entities.Role;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.repositories.RoleRepository;
import dev.luisghtz.myaichat.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Transactional
    public User createOrUpdateUserFromGitHub(Map<String, Object> githubUser) {
        String githubId = String.valueOf(githubUser.get("id"));
        String email = (String) githubUser.get("email");
        String username = (String) githubUser.get("login");
        String avatarUrl = (String) githubUser.get("avatar_url");
        
        // Try to find existing user by GitHub ID or email
        Optional<User> existingUser = userRepository.findByGithubId(githubId);
        if (existingUser.isEmpty() && email != null) {
            existingUser = userRepository.findByEmail(email);
        }
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update user information
            user.setGithubId(githubId);
            user.setAvatarUrl(avatarUrl);
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
            }
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(generateUniqueUsername(username));
            }
        } else {
            // Create new user
            user = new User();
            user.setGithubId(githubId);
            user.setEmail(email != null ? email : generateEmailFromUsername(username));
            user.setUsername(generateUniqueUsername(username));
            user.setAvatarUrl(avatarUrl);
            user.setLocked(false);
            user.setDisabled(false);
            
            // Assign default role
            Role userRole = roleRepository.findByName(Role.RoleName.USER)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRole(userRole);
        }
        
        return userRepository.save(user);
    }
    
    public UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleName(user.getRole().getName().name());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setLocked(user.getLocked());
        dto.setDisabled(user.getDisabled());
        return dto;
    }
    
    private String generateUniqueUsername(String baseUsername) {
        if (baseUsername == null || baseUsername.trim().isEmpty()) {
            baseUsername = "user";
        }
        
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    private String generateEmailFromUsername(String username) {
        return username + "@github.local";
    }
    
    public Optional<User> findById(String userId) {
        try {
            return userRepository.findById(java.util.UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", userId);
            return Optional.empty();
        }
    }
}
