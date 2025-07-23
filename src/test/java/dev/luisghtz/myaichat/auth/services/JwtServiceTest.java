package dev.luisghtz.myaichat.auth.services;

import dev.luisghtz.myaichat.auth.entities.Role;
import dev.luisghtz.myaichat.auth.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private String testSecret = "test-secret-key-for-jwt-token-generation-and-validation";
    private long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        // Setup test user
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(userRole);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("generateToken - Should generate valid JWT token for user")
        void testGenerateToken_ShouldGenerateValidJwtTokenForUser() {
            // Act
            String token = jwtService.generateToken(testUser);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.contains("."));
            
            // Verify the token can be validated
            assertTrue(jwtService.validateToken(token));
        }

        @Test
        @DisplayName("generateToken - Should generate token with correct user ID")
        void testGenerateToken_ShouldGenerateTokenWithCorrectUserId() {
            // Act
            String token = jwtService.generateToken(testUser);
            String extractedUserId = jwtService.getUserIdFromToken(token);

            // Assert
            assertNotNull(extractedUserId);
            assertEquals(testUser.getId().toString(), extractedUserId);
        }

        @Test
        @DisplayName("generateToken - Should generate token with correct username")
        void testGenerateToken_ShouldGenerateTokenWithCorrectUsername() {
            // Act
            String token = jwtService.generateToken(testUser);
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertNotNull(extractedUsername);
            assertEquals(testUser.getUsername(), extractedUsername);
        }

        @Test
        @DisplayName("generateToken - Should generate different tokens for different users")
        void testGenerateToken_ShouldGenerateDifferentTokensForDifferentUsers() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());
            anotherUser.setUsername("anotheruser");
            anotherUser.setEmail("another@example.com");
            anotherUser.setRole(testUser.getRole());

            // Act
            String token1 = jwtService.generateToken(testUser);
            String token2 = jwtService.generateToken(anotherUser);

            // Assert
            assertNotNull(token1);
            assertNotNull(token2);
            assertNotEquals(token1, token2);
        }

        @Test
        @DisplayName("generateToken - Should generate token with ADMIN role")
        void testGenerateToken_ShouldGenerateTokenWithAdminRole() {
            // Arrange
            Role adminRole = new Role();
            adminRole.setId(2L);
            adminRole.setName(Role.RoleName.ADMIN);
            testUser.setRole(adminRole);

            // Act
            String token = jwtService.generateToken(testUser);

            // Assert
            assertNotNull(token);
            assertTrue(jwtService.validateToken(token));
            assertEquals(testUser.getId().toString(), jwtService.getUserIdFromToken(token));
        }

        @Test
        @DisplayName("generateToken - Should handle null user gracefully")
        void testGenerateToken_ShouldHandleNullUserGracefully() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> jwtService.generateToken(null));
        }

        @Test
        @DisplayName("generateToken - Should handle user with null fields")
        void testGenerateToken_ShouldHandleUserWithNullFields() {
            // Arrange
            User userWithNullFields = new User();
            userWithNullFields.setId(UUID.randomUUID());
            userWithNullFields.setUsername(null);
            userWithNullFields.setEmail(null);
            userWithNullFields.setRole(testUser.getRole());

            // Act
            String token = jwtService.generateToken(userWithNullFields);

            // Assert
            assertNotNull(token);
            assertTrue(jwtService.validateToken(token));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("validateToken - Should return true for valid token")
        void testValidateToken_ShouldReturnTrueForValidToken() {
            // Arrange
            String token = jwtService.generateToken(testUser);

            // Act
            boolean isValid = jwtService.validateToken(token);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("validateToken - Should return false for invalid token")
        void testValidateToken_ShouldReturnFalseForInvalidToken() {
            // Arrange
            String invalidToken = "invalid.jwt.token";

            // Act
            boolean isValid = jwtService.validateToken(invalidToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("validateToken - Should return false for malformed token")
        void testValidateToken_ShouldReturnFalseForMalformedToken() {
            // Arrange
            String malformedToken = "not.a.valid.jwt.token.structure";

            // Act
            boolean isValid = jwtService.validateToken(malformedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("validateToken - Should return false for null token")
        void testValidateToken_ShouldReturnFalseForNullToken() {
            // Act
            boolean isValid = jwtService.validateToken(null);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("validateToken - Should return false for empty token")
        void testValidateToken_ShouldReturnFalseForEmptyToken() {
            // Act
            boolean isValid = jwtService.validateToken("");

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("validateToken - Should return false for token with wrong signature")
        void testValidateToken_ShouldReturnFalseForTokenWithWrongSignature() {
            // Arrange
            String token = jwtService.generateToken(testUser);
            // Change the secret to simulate wrong signature
            ReflectionTestUtils.setField(jwtService, "jwtSecret", "different-secret");

            // Act
            boolean isValid = jwtService.validateToken(token);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("validateToken - Should return false for expired token")
        void testValidateToken_ShouldReturnFalseForExpiredToken() {
            // Arrange
            // Set expiration to a negative value to create an expired token
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
            String expiredToken = jwtService.generateToken(testUser);
            
            // Reset expiration to normal
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

            // Act
            boolean isValid = jwtService.validateToken(expiredToken);

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Get User ID From Token Tests")
    class GetUserIdFromTokenTests {

        @Test
        @DisplayName("getUserIdFromToken - Should extract correct user ID from valid token")
        void testGetUserIdFromToken_ShouldExtractCorrectUserIdFromValidToken() {
            // Arrange
            String token = jwtService.generateToken(testUser);

            // Act
            String extractedUserId = jwtService.getUserIdFromToken(token);

            // Assert
            assertNotNull(extractedUserId);
            assertEquals(testUser.getId().toString(), extractedUserId);
        }

        @Test
        @DisplayName("getUserIdFromToken - Should return null for invalid token")
        void testGetUserIdFromToken_ShouldReturnNullForInvalidToken() {
            // Arrange
            String invalidToken = "invalid.jwt.token";

            // Act
            String extractedUserId = jwtService.getUserIdFromToken(invalidToken);

            // Assert
            assertNull(extractedUserId);
        }

        @Test
        @DisplayName("getUserIdFromToken - Should return null for null token")
        void testGetUserIdFromToken_ShouldReturnNullForNullToken() {
            // Act
            String extractedUserId = jwtService.getUserIdFromToken(null);

            // Assert
            assertNull(extractedUserId);
        }

        @Test
        @DisplayName("getUserIdFromToken - Should return null for empty token")
        void testGetUserIdFromToken_ShouldReturnNullForEmptyToken() {
            // Act
            String extractedUserId = jwtService.getUserIdFromToken("");

            // Assert
            assertNull(extractedUserId);
        }

        @Test
        @DisplayName("getUserIdFromToken - Should return null for malformed token")
        void testGetUserIdFromToken_ShouldReturnNullForMalformedToken() {
            // Arrange
            String malformedToken = "not.a.valid.jwt.token.structure";

            // Act
            String extractedUserId = jwtService.getUserIdFromToken(malformedToken);

            // Assert
            assertNull(extractedUserId);
        }
    }

    @Nested
    @DisplayName("Get Username From Token Tests")
    class GetUsernameFromTokenTests {

        @Test
        @DisplayName("getUsernameFromToken - Should extract correct username from valid token")
        void testGetUsernameFromToken_ShouldExtractCorrectUsernameFromValidToken() {
            // Arrange
            String token = jwtService.generateToken(testUser);

            // Act
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertNotNull(extractedUsername);
            assertEquals(testUser.getUsername(), extractedUsername);
        }

        @Test
        @DisplayName("getUsernameFromToken - Should return null for invalid token")
        void testGetUsernameFromToken_ShouldReturnNullForInvalidToken() {
            // Arrange
            String invalidToken = "invalid.jwt.token";

            // Act
            String extractedUsername = jwtService.getUsernameFromToken(invalidToken);

            // Assert
            assertNull(extractedUsername);
        }

        @Test
        @DisplayName("getUsernameFromToken - Should return null for null token")
        void testGetUsernameFromToken_ShouldReturnNullForNullToken() {
            // Act
            String extractedUsername = jwtService.getUsernameFromToken(null);

            // Assert
            assertNull(extractedUsername);
        }

        @Test
        @DisplayName("getUsernameFromToken - Should return null for empty token")
        void testGetUsernameFromToken_ShouldReturnNullForEmptyToken() {
            // Act
            String extractedUsername = jwtService.getUsernameFromToken("");

            // Assert
            assertNull(extractedUsername);
        }

        @Test
        @DisplayName("getUsernameFromToken - Should handle token with null username claim")
        void testGetUsernameFromToken_ShouldHandleTokenWithNullUsernameClaim() {
            // Arrange
            User userWithNullUsername = new User();
            userWithNullUsername.setId(UUID.randomUUID());
            userWithNullUsername.setUsername(null);
            userWithNullUsername.setEmail("test@example.com");
            userWithNullUsername.setRole(testUser.getRole());
            
            String token = jwtService.generateToken(userWithNullUsername);

            // Act
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertNull(extractedUsername);
        }
    }

    @Nested
    @DisplayName("Token Security Tests")
    class TokenSecurityTests {

        @Test
        @DisplayName("Token security - Generated tokens should be different each time")
        void testTokenSecurity_GeneratedTokensShouldBeDifferentEachTime() throws InterruptedException {
            // Act
            String token1 = jwtService.generateToken(testUser);
            Thread.sleep(1); // Ensure different timestamps
            String token2 = jwtService.generateToken(testUser);

            // Assert
            assertNotEquals(token1, token2);
        }

        @Test
        @DisplayName("Token security - Should not validate token with different secret")
        void testTokenSecurity_ShouldNotValidateTokenWithDifferentSecret() {
            // Arrange
            String token = jwtService.generateToken(testUser);
            
            // Create new service with different secret
            JwtService differentSecretService = new JwtService();
            ReflectionTestUtils.setField(differentSecretService, "jwtSecret", "different-secret");
            ReflectionTestUtils.setField(differentSecretService, "jwtExpiration", testExpiration);

            // Act
            boolean isValid = differentSecretService.validateToken(token);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Token security - Should not extract data from token with different secret")
        void testTokenSecurity_ShouldNotExtractDataFromTokenWithDifferentSecret() {
            // Arrange
            String token = jwtService.generateToken(testUser);
            
            // Create new service with different secret
            JwtService differentSecretService = new JwtService();
            ReflectionTestUtils.setField(differentSecretService, "jwtSecret", "different-secret");
            ReflectionTestUtils.setField(differentSecretService, "jwtExpiration", testExpiration);

            // Act
            String extractedUserId = differentSecretService.getUserIdFromToken(token);
            String extractedUsername = differentSecretService.getUsernameFromToken(token);

            // Assert
            assertNull(extractedUserId);
            assertNull(extractedUsername);
        }
    }
}
