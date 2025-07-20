package dev.luisghtz.myaichat.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.entities.Role;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.AuthService;
import dev.luisghtz.myaichat.auth.services.JwtService;
import dev.luisghtz.myaichat.auth.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;
    
    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private org.springframework.ai.openai.OpenAiChatModel openAiChatModel;
    
    @MockitoBean
    private org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel vertexAiChatModel;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserDto testUserDto;
    private String validToken;
    private String invalidToken;

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

        validToken = "valid.jwt.token";
        invalidToken = "invalid.jwt.token";
    }

    @Nested
    @DisplayName("GET /api/auth/login")
    class GetLoginEndpoints {

        @Test
        @DisplayName("GET /api/auth/login - Should return login information with GitHub OAuth URL")
        void testLogin_ShouldReturnLoginInformation() throws Exception {
            mockMvc.perform(get("/api/auth/login"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Please initiate OAuth2 login"))
                    .andExpect(jsonPath("$.github_login_url").value("/oauth2/authorization/github"));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/status")
    class GetStatusEndpoints {

        @Test
        @DisplayName("GET /api/auth/status - Should return service status information")
        void testStatus_ShouldReturnServiceStatus() throws Exception {
            mockMvc.perform(get("/api/auth/status"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("Authentication service is running"))
                    .andExpect(jsonPath("$.version").value("1.0.0"));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/user")
    class GetUserEndpoints {

        @Test
        @DisplayName("GET /api/auth/user - Should return current user when valid token provided")
        void testGetCurrentUser_ShouldReturnUserWhenValidToken() throws Exception {
            when(authService.getUserFromToken(validToken)).thenReturn(testUser);
            when(userService.convertToDto(testUser)).thenReturn(testUserDto);

            mockMvc.perform(get("/api/auth/user")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(testUserDto.getId().toString()))
                    .andExpect(jsonPath("$.username").value(testUserDto.getUsername()))
                    .andExpect(jsonPath("$.email").value(testUserDto.getEmail()))
                    .andExpect(jsonPath("$.roleName").value(testUserDto.getRoleName()))
                    .andExpect(jsonPath("$.avatarUrl").value(testUserDto.getAvatarUrl()));
        }

        @Test
        @DisplayName("GET /api/auth/user - Should return 404 when user not found")
        void testGetCurrentUser_ShouldReturn404WhenUserNotFound() throws Exception {
            when(authService.getUserFromToken(validToken)).thenReturn(null);

            mockMvc.perform(get("/api/auth/user")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/auth/user - Should return 400 when no Authorization header")
        void testGetCurrentUser_ShouldReturn400WhenNoAuthHeader() throws Exception {
            mockMvc.perform(get("/api/auth/user"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/auth/user - Should return 400 when invalid Authorization header format")
        void testGetCurrentUser_ShouldReturn400WhenInvalidAuthHeaderFormat() throws Exception {
            mockMvc.perform(get("/api/auth/user")
                    .header("Authorization", "InvalidFormat"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/auth/user - Should return 400 when service throws exception")
        void testGetCurrentUser_ShouldReturn400WhenServiceThrowsException() throws Exception {
            when(authService.getUserFromToken(anyString())).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/auth/user")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/validate")
    class PostValidateEndpoints {

        @Test
        @DisplayName("POST /api/auth/validate - Should return valid true when token is valid and user exists")
        void testValidateToken_ShouldReturnValidTrueWhenTokenValidAndUserExists() throws Exception {
            when(authService.validateToken(validToken)).thenReturn(true);
            when(authService.getUserFromToken(validToken)).thenReturn(testUser);
            when(userService.convertToDto(testUser)).thenReturn(testUserDto);

            mockMvc.perform(post("/api/auth/validate")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.user.id").value(testUserDto.getId().toString()))
                    .andExpect(jsonPath("$.user.username").value(testUserDto.getUsername()));
        }

        @Test
        @DisplayName("POST /api/auth/validate - Should return valid false when token is invalid")
        void testValidateToken_ShouldReturnValidFalseWhenTokenInvalid() throws Exception {
            when(authService.validateToken(invalidToken)).thenReturn(false);

            mockMvc.perform(post("/api/auth/validate")
                    .header("Authorization", "Bearer " + invalidToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid token"));
        }

        @Test
        @DisplayName("POST /api/auth/validate - Should return valid false when token is valid but user not found")
        void testValidateToken_ShouldReturnValidFalseWhenTokenValidButUserNotFound() throws Exception {
            when(authService.validateToken(validToken)).thenReturn(true);
            when(authService.getUserFromToken(validToken)).thenReturn(null);

            mockMvc.perform(post("/api/auth/validate")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid token"));
        }

        @Test
        @DisplayName("POST /api/auth/validate - Should return bad request when no Authorization header")
        void testValidateToken_ShouldReturnBadRequestWhenNoAuthHeader() throws Exception {
            mockMvc.perform(post("/api/auth/validate"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid token format"));
        }

        @Test
        @DisplayName("POST /api/auth/validate - Should return bad request when invalid Authorization header format")
        void testValidateToken_ShouldReturnBadRequestWhenInvalidAuthHeaderFormat() throws Exception {
            mockMvc.perform(post("/api/auth/validate")
                    .header("Authorization", "InvalidFormat"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid token format"));
        }

        @Test
        @DisplayName("POST /api/auth/validate - Should return valid false when service throws exception")
        void testValidateToken_ShouldReturnValidFalseWhenServiceThrowsException() throws Exception {
            when(authService.validateToken(anyString())).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(post("/api/auth/validate")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Token validation failed"));
        }
    }
}
