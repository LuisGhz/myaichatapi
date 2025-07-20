package dev.luisghtz.myaichat.auth.services;

import dev.luisghtz.myaichat.auth.dtos.AuthResponse;
import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.entities.Role;
import dev.luisghtz.myaichat.auth.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDto testUserDto;
    private String testToken;
    private Map<String, Object> githubUserData;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setGithubId("123456");
        testUser.setAvatarUrl("https://github.com/avatar.jpg");
        testUser.setLocked(false);
        testUser.setDisabled(false);
        
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);
        testUser.setRole(userRole);

        testUserDto = new UserDto();
        testUserDto.setId(testUser.getId());
        testUserDto.setUsername(testUser.getUsername());
        testUserDto.setEmail(testUser.getEmail());
        testUserDto.setRoleName("USER");
        testUserDto.setAvatarUrl(testUser.getAvatarUrl());
        testUserDto.setLocked(false);
        testUserDto.setDisabled(false);

        testToken = "valid.jwt.token";

        githubUserData = new HashMap<>();
        githubUserData.put("id", 123456);
        githubUserData.put("login", "testuser");
        githubUserData.put("email", "test@example.com");
        githubUserData.put("avatar_url", "https://github.com/avatar.jpg");
    }

    @Nested
    @DisplayName("GitHub Authentication Tests")
    class GitHubAuthenticationTests {

        @Test
        @DisplayName("authenticateWithGitHub - Should successfully authenticate user and return AuthResponse")
        void testAuthenticateWithGitHub_ShouldSuccessfullyAuthenticateUser() {
            // Arrange
            when(userService.createOrUpdateUserFromGitHub(githubUserData)).thenReturn(testUser);
            when(jwtService.generateToken(testUser)).thenReturn(testToken);
            when(userService.convertToDto(testUser)).thenReturn(testUserDto);

            // Act
            AuthResponse result = authService.authenticateWithGitHub(githubUserData);

            // Assert
            assertNotNull(result);
            assertEquals(testToken, result.getToken());
            assertEquals("Bearer", result.getType());
            assertEquals(testUserDto, result.getUser());
            
            verify(userService).createOrUpdateUserFromGitHub(githubUserData);
            verify(jwtService).generateToken(testUser);
            verify(userService).convertToDto(testUser);
        }

        @Test
        @DisplayName("authenticateWithGitHub - Should handle null GitHub user data")
        void testAuthenticateWithGitHub_ShouldHandleNullGitHubUserData() {
            // Arrange
            when(userService.createOrUpdateUserFromGitHub(null)).thenThrow(new RuntimeException("Invalid GitHub user data"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.authenticateWithGitHub(null));
            
            verify(userService).createOrUpdateUserFromGitHub(null);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("authenticateWithGitHub - Should handle user service exception")
        void testAuthenticateWithGitHub_ShouldHandleUserServiceException() {
            // Arrange
            when(userService.createOrUpdateUserFromGitHub(githubUserData))
                    .thenThrow(new RuntimeException("User creation failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.authenticateWithGitHub(githubUserData));
            
            verify(userService).createOrUpdateUserFromGitHub(githubUserData);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("authenticateWithGitHub - Should handle JWT service exception")
        void testAuthenticateWithGitHub_ShouldHandleJwtServiceException() {
            // Arrange
            when(userService.createOrUpdateUserFromGitHub(githubUserData)).thenReturn(testUser);
            when(jwtService.generateToken(testUser)).thenThrow(new RuntimeException("Token generation failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.authenticateWithGitHub(githubUserData));
            
            verify(userService).createOrUpdateUserFromGitHub(githubUserData);
            verify(jwtService).generateToken(testUser);
            verify(userService, never()).convertToDto(any());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("validateToken - Should return true when token is valid")
        void testValidateToken_ShouldReturnTrueWhenTokenIsValid() {
            // Arrange
            when(jwtService.validateToken(testToken)).thenReturn(true);

            // Act
            boolean result = authService.validateToken(testToken);

            // Assert
            assertTrue(result);
            verify(jwtService).validateToken(testToken);
        }

        @Test
        @DisplayName("validateToken - Should return false when token is invalid")
        void testValidateToken_ShouldReturnFalseWhenTokenIsInvalid() {
            // Arrange
            String invalidToken = "invalid.token";
            when(jwtService.validateToken(invalidToken)).thenReturn(false);

            // Act
            boolean result = authService.validateToken(invalidToken);

            // Assert
            assertFalse(result);
            verify(jwtService).validateToken(invalidToken);
        }

        @Test
        @DisplayName("validateToken - Should return false when JWT service throws exception")
        void testValidateToken_ShouldReturnFalseWhenJwtServiceThrowsException() {
            // Arrange
            when(jwtService.validateToken(testToken)).thenThrow(new RuntimeException("Validation error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.validateToken(testToken));
            
            verify(jwtService).validateToken(testToken);
        }

        @Test
        @DisplayName("validateToken - Should handle null token")
        void testValidateToken_ShouldHandleNullToken() {
            // Arrange
            when(jwtService.validateToken(null)).thenReturn(false);

            // Act
            boolean result = authService.validateToken(null);

            // Assert
            assertFalse(result);
            verify(jwtService).validateToken(null);
        }

        @Test
        @DisplayName("validateToken - Should handle empty token")
        void testValidateToken_ShouldHandleEmptyToken() {
            // Arrange
            String emptyToken = "";
            when(jwtService.validateToken(emptyToken)).thenReturn(false);

            // Act
            boolean result = authService.validateToken(emptyToken);

            // Assert
            assertFalse(result);
            verify(jwtService).validateToken(emptyToken);
        }
    }

    @Nested
    @DisplayName("Get User From Token Tests")
    class GetUserFromTokenTests {

        @Test
        @DisplayName("getUserFromToken - Should return user when token is valid and user exists")
        void testGetUserFromToken_ShouldReturnUserWhenTokenValidAndUserExists() {
            // Arrange
            when(jwtService.getUserIdFromToken(testToken)).thenReturn(testUser.getId());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.of(testUser));

            // Act
            User result = authService.getUserFromToken(testToken);

            // Assert
            assertNotNull(result);
            assertEquals(testUser, result);
            
            verify(jwtService).getUserIdFromToken(testToken);
            verify(userService).findById(testUser.getId().toString());
        }

        @Test
        @DisplayName("getUserFromToken - Should return null when token is invalid")
        void testGetUserFromToken_ShouldReturnNullWhenTokenIsInvalid() {
            // Arrange
            when(jwtService.getUserIdFromToken(testToken)).thenReturn(null);

            // Act
            User result = authService.getUserFromToken(testToken);

            // Assert
            assertNull(result);
            
            verify(jwtService).getUserIdFromToken(testToken);
            verify(userService).findById("null");
        }

        @Test
        @DisplayName("getUserFromToken - Should return null when user not found")
        void testGetUserFromToken_ShouldReturnNullWhenUserNotFound() {
            // Arrange
            when(jwtService.getUserIdFromToken(testToken)).thenReturn(testUser.getId());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.empty());

            // Act
            User result = authService.getUserFromToken(testToken);

            // Assert
            assertNull(result);
            
            verify(jwtService).getUserIdFromToken(testToken);
            verify(userService).findById(testUser.getId().toString());
        }

        @Test
        @DisplayName("getUserFromToken - Should handle JWT service exception")
        void testGetUserFromToken_ShouldHandleJwtServiceException() {
            // Arrange
            when(jwtService.getUserIdFromToken(testToken)).thenThrow(new RuntimeException("Token parsing error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.getUserFromToken(testToken));
            
            verify(jwtService).getUserIdFromToken(testToken);
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("getUserFromToken - Should handle user service exception")
        void testGetUserFromToken_ShouldHandleUserServiceException() {
            // Arrange
            when(jwtService.getUserIdFromToken(testToken)).thenReturn(testUser.getId());
            when(userService.findById(anyString())).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> authService.getUserFromToken(testToken));
            
            verify(jwtService).getUserIdFromToken(testToken);
            verify(userService).findById(testUser.getId().toString());
        }

        @Test
        @DisplayName("getUserFromToken - Should handle null token")
        void testGetUserFromToken_ShouldHandleNullToken() {
            // Arrange
            when(jwtService.getUserIdFromToken(null)).thenReturn(null);

            // Act
            User result = authService.getUserFromToken(null);

            // Assert
            assertNull(result);
            
            verify(jwtService).getUserIdFromToken(null);
            verify(userService).findById("null");
        }
    }
}
