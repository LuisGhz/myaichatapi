package dev.luisghtz.myaichat.auth.utils;

import dev.luisghtz.myaichat.auth.entities.Role;
import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.JwtService;
import dev.luisghtz.myaichat.auth.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();

        // Setup test data
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(userRole);
        testUser.setLocked(false);
        testUser.setDisabled(false);

        validToken = "valid.jwt.token";
        invalidToken = "invalid.jwt.token";
    }

    @Nested
    @DisplayName("Authentication Filter Processing Tests")
    class AuthenticationFilterProcessingTests {

        @Test
        @DisplayName("doFilterInternal - Should authenticate user when valid token provided")
        void testDoFilterInternal_ShouldAuthenticateUserWhenValidTokenProvided() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.of(testUser));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(testUser, auth.getPrincipal());
            assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
            
            verify(filterChain).doFilter(request, response);
            verify(jwtService).validateToken(validToken);
            verify(jwtService).getUserIdFromToken(validToken);
            verify(userService).findById(testUser.getId().toString());
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when no Authorization header")
        void testDoFilterInternal_ShouldNotAuthenticateWhenNoAuthorizationHeader() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService, userService);
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when Authorization header doesn't start with Bearer")
        void testDoFilterInternal_ShouldNotAuthenticateWhenAuthHeaderDoesNotStartWithBearer() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService, userService);
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when token is invalid")
        void testDoFilterInternal_ShouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
            when(jwtService.validateToken(invalidToken)).thenReturn(false);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verify(jwtService).validateToken(invalidToken);
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when user ID cannot be extracted from token")
        void testDoFilterInternal_ShouldNotAuthenticateWhenUserIdCannotBeExtracted() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verify(jwtService).validateToken(validToken);
            verify(jwtService).getUserIdFromToken(validToken);
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when user not found")
        void testDoFilterInternal_ShouldNotAuthenticateWhenUserNotFound() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.empty());

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verify(jwtService).validateToken(validToken);
            verify(jwtService).getUserIdFromToken(validToken);
            verify(userService).findById(testUser.getId().toString());
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when user is locked")
        void testDoFilterInternal_ShouldNotAuthenticateWhenUserIsLocked() throws ServletException, IOException {
            // Arrange
            testUser.setLocked(true);
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.of(testUser));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when user is disabled")
        void testDoFilterInternal_ShouldNotAuthenticateWhenUserIsDisabled() throws ServletException, IOException {
            // Arrange
            testUser.setDisabled(true);
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.of(testUser));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("User Role Authentication Tests")
    class UserRoleAuthenticationTests {

        @Test
        @DisplayName("doFilterInternal - Should authenticate admin user with ADMIN role")
        void testDoFilterInternal_ShouldAuthenticateAdminUserWithAdminRole() throws ServletException, IOException {
            // Arrange
            Role adminRole = new Role();
            adminRole.setId(2L);
            adminRole.setName(Role.RoleName.ADMIN);
            testUser.setRole(adminRole);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.of(testUser));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(testUser, auth.getPrincipal());
            assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
            
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("doFilterInternal - Should set correct authorities for USER role")
        void testDoFilterInternal_ShouldSetCorrectAuthoritiesForUserRole() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(testUser.getId().toString())).thenReturn(Optional.of(testUser));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(1, auth.getAuthorities().size());
            assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
            
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Security Context Tests")
    class SecurityContextTests {

        @Test
        @DisplayName("doFilterInternal - Should not authenticate when security context already has authentication")
        void testDoFilterInternal_ShouldNotAuthenticateWhenSecurityContextAlreadyHasAuthentication() throws ServletException, IOException {
            // Arrange
            // Set existing authentication in security context
            Authentication existingAuth = mock(Authentication.class);
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertEquals(existingAuth, auth); // Should still be the existing auth
            
            verify(filterChain).doFilter(request, response);
            verify(jwtService).validateToken(validToken);
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("doFilterInternal - Should clear security context when authentication fails")
        void testDoFilterInternal_ShouldClearSecurityContextWhenAuthenticationFails() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
            when(jwtService.validateToken(invalidToken)).thenReturn(false);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("doFilterInternal - Should handle JWT service exception gracefully")
        void testDoFilterInternal_ShouldHandleJwtServiceExceptionGracefully() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenThrow(new RuntimeException("JWT processing error"));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verify(jwtService).validateToken(validToken);
        }

        @Test
        @DisplayName("doFilterInternal - Should handle user service exception gracefully")
        void testDoFilterInternal_ShouldHandleUserServiceExceptionGracefully() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.getUserIdFromToken(validToken)).thenReturn(testUser.getId().toString());
            when(userService.findById(anyString())).thenThrow(new RuntimeException("Database error"));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("doFilterInternal - Should continue filter chain even when exception occurs")
        void testDoFilterInternal_ShouldContinueFilterChainEvenWhenExceptionOccurs() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.validateToken(validToken)).thenThrow(new RuntimeException("Unexpected error"));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("doFilterInternal - Should handle empty Bearer token")
        void testDoFilterInternal_ShouldHandleEmptyBearerToken() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer ");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("doFilterInternal - Should handle Bearer with only whitespace")
        void testDoFilterInternal_ShouldHandleBearerWithOnlyWhitespace() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("Bearer    ");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("doFilterInternal - Should handle case-sensitive Bearer token")
        void testDoFilterInternal_ShouldHandleCaseSensitiveBearerToken() throws ServletException, IOException {
            // Arrange
            when(request.getHeader("Authorization")).thenReturn("bearer " + validToken);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService, userService);
        }
    }
}
