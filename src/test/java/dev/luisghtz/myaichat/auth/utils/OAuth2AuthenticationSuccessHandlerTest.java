package dev.luisghtz.myaichat.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisghtz.myaichat.auth.dtos.AuthResponse;
import dev.luisghtz.myaichat.auth.dtos.UserDto;
import dev.luisghtz.myaichat.auth.services.AuthService;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2AuthenticationSuccessHandler Tests")
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private AuthService authService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OAuth2AuthenticationToken oAuth2Token;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private Map<String, Object> githubUserAttributes;
    private AuthResponse authResponse;
    private UserDto userDto;
    private String successRedirectUrl;

    @BeforeEach
    void setUp() {
        successRedirectUrl = "http://localhost:3000/auth/success";
        ReflectionTestUtils.setField(successHandler, "successRedirectUrl", successRedirectUrl);

        // Setup GitHub user attributes
        githubUserAttributes = new HashMap<>();
        githubUserAttributes.put("id", 123456);
        githubUserAttributes.put("login", "testuser");
        githubUserAttributes.put("email", "test@example.com");
        githubUserAttributes.put("avatar_url", "https://github.com/avatar.jpg");

        // Setup UserDto
        userDto = new UserDto();
        userDto.setId(UUID.randomUUID());
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setRoleName("USER");
        userDto.setAvatarUrl("https://github.com/avatar.jpg");
        userDto.setLocked(false);
        userDto.setDisabled(false);

        // Setup AuthResponse
        authResponse = new AuthResponse();
        authResponse.setToken("generated.jwt.token");
        authResponse.setType("Bearer");
        authResponse.setUser(userDto);
    }

    @Nested
    @DisplayName("Successful Authentication Tests")
    class SuccessfulAuthenticationTests {

        @Test
        @DisplayName("onAuthenticationSuccess - Should process GitHub OAuth2 authentication successfully")
        void testOnAuthenticationSuccess_ShouldProcessGitHubOAuth2AuthenticationSuccessfully() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(oAuth2Token).getPrincipal();
            verify(oAuth2Token).getAuthorizedClientRegistrationId();
            verify(oAuth2User, times(2)).getAttributes(); // Called twice - once for logging, once for processing
            verify(authService).authenticateWithGitHub(githubUserAttributes);
            verify(response).sendRedirect(successRedirectUrl + "?token=" + authResponse.getToken());
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle GitHub provider with case insensitive matching")
        void testOnAuthenticationSuccess_ShouldHandleGitHubProviderWithCaseInsensitiveMatching() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("GITHUB");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(authService).authenticateWithGitHub(githubUserAttributes);
            verify(response).sendRedirect(successRedirectUrl + "?token=" + authResponse.getToken());
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should redirect with token in query parameter")
        void testOnAuthenticationSuccess_ShouldRedirectWithTokenInQueryParameter() throws IOException {
            // Arrange
            String expectedToken = "test.jwt.token.here";
            authResponse.setToken(expectedToken);
            
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            String expectedRedirectUrl = successRedirectUrl + "?token=" + expectedToken;
            verify(response).sendRedirect(expectedRedirectUrl);
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should process user attributes correctly")
        void testOnAuthenticationSuccess_ShouldProcessUserAttributesCorrectly() throws IOException {
            // Arrange
            Map<String, Object> customAttributes = new HashMap<>();
            customAttributes.put("id", 987654);
            customAttributes.put("login", "customuser");
            customAttributes.put("email", "custom@example.com");
            customAttributes.put("avatar_url", "https://github.com/custom-avatar.jpg");

            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(customAttributes);
            when(authService.authenticateWithGitHub(customAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(authService).authenticateWithGitHub(customAttributes);
            verify(response).sendRedirect(anyString());
        }
    }

    @Nested
    @DisplayName("Unsupported Provider Tests")
    class UnsupportedProviderTests {

        @Test
        @DisplayName("onAuthenticationSuccess - Should redirect with error when unsupported provider")
        void testOnAuthenticationSuccess_ShouldRedirectWithErrorWhenUnsupportedProvider() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("google");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect(successRedirectUrl + "?error=authentication_failed");
            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should redirect with error when null provider")
        void testOnAuthenticationSuccess_ShouldRedirectWithErrorWhenNullProvider() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn(null);
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect(successRedirectUrl + "?error=authentication_failed");
            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should redirect with error when empty provider")
        void testOnAuthenticationSuccess_ShouldRedirectWithErrorWhenEmptyProvider() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect(successRedirectUrl + "?error=authentication_failed");
            verifyNoInteractions(authService);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("onAuthenticationSuccess - Should redirect with error when AuthService throws exception")
        void testOnAuthenticationSuccess_ShouldRedirectWithErrorWhenAuthServiceThrowsException() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes))
                    .thenThrow(new RuntimeException("Authentication failed"));

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(authService).authenticateWithGitHub(githubUserAttributes);
            verify(response).sendRedirect(successRedirectUrl + "?error=authentication_failed");
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle null OAuth2User gracefully")
        void testOnAuthenticationSuccess_ShouldHandleNullOAuth2UserGracefully() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(null);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect(successRedirectUrl + "?error=user_not_found");
            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle null user attributes gracefully")
        void testOnAuthenticationSuccess_ShouldHandleNullUserAttributesGracefully() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(null);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect(successRedirectUrl + "?error=authentication_failed");
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle empty user attributes gracefully")
        void testOnAuthenticationSuccess_ShouldHandleEmptyUserAttributesGracefully() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(new HashMap<>());
            when(authService.authenticateWithGitHub(any())).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(authService).authenticateWithGitHub(any(Map.class));
            verify(response).sendRedirect(successRedirectUrl + "?token=" + authResponse.getToken());
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle IOException during redirect")
        void testOnAuthenticationSuccess_ShouldHandleIOExceptionDuringRedirect() throws IOException {
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);
            doThrow(new IOException("Redirect failed")).when(response).sendRedirect(anyString());

            // Act & Assert
            // The method doesn't handle IOException, so it should propagate
            org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> 
                successHandler.onAuthenticationSuccess(request, response, oAuth2Token));
            
            verify(authService).authenticateWithGitHub(githubUserAttributes);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("onAuthenticationSuccess - Should use configured redirect URL")
        void testOnAuthenticationSuccess_ShouldUseConfiguredRedirectUrl() throws IOException {
            // Arrange
            String customRedirectUrl = "http://example.com/auth/callback";
            ReflectionTestUtils.setField(successHandler, "successRedirectUrl", customRedirectUrl);

            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect(customRedirectUrl + "?token=" + authResponse.getToken());
        }

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle null redirect URL configuration")
        void testOnAuthenticationSuccess_ShouldHandleNullRedirectUrlConfiguration() throws IOException {
            // Arrange
            ReflectionTestUtils.setField(successHandler, "successRedirectUrl", null);

            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(response).sendRedirect("null?token=" + authResponse.getToken());
        }
    }

    @Nested
    @DisplayName("Authentication Token Tests")
    class AuthenticationTokenTests {

        @Test
        @DisplayName("onAuthenticationSuccess - Should handle different OAuth2AuthenticationToken implementations")
        void testOnAuthenticationSuccess_ShouldHandleDifferentOAuth2AuthenticationTokenImplementations() throws IOException {
            // Test is inherently covered by using mock OAuth2AuthenticationToken
            // This test verifies the handler works with the OAuth2AuthenticationToken interface
            
            // Arrange
            when(oAuth2Token.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2Token.getAuthorizedClientRegistrationId()).thenReturn("github");
            when(oAuth2User.getAttributes()).thenReturn(githubUserAttributes);
            when(authService.authenticateWithGitHub(githubUserAttributes)).thenReturn(authResponse);

            // Act
            successHandler.onAuthenticationSuccess(request, response, oAuth2Token);

            // Assert
            verify(oAuth2Token).getPrincipal();
            verify(oAuth2Token).getAuthorizedClientRegistrationId();
            verify(response).sendRedirect(anyString());
        }
    }
}
