package dev.luisghtz.myaichat.auth.utils;

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationFailureHandler failureHandler;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationException exception;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(failureHandler, "successRedirectUrl", "http://localhost/success");
    }

    @Nested
    @DisplayName("onAuthenticationFailure method")
    class OnAuthenticationFailureTests {

        @Test
        @DisplayName("Should log details and redirect to login with error param on generic AuthenticationException")
        void shouldLogAndRedirectOnGenericException() throws IOException {
            when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/oauth2/callback"));
            when(request.getScheme()).thenReturn("https");
            when(request.getServerName()).thenReturn("localhost");
            when(request.getServerPort()).thenReturn(8080);
            when(request.getRequestURI()).thenReturn("/api/oauth2/callback");
            when(request.getQueryString()).thenReturn("code=123");
            when(request.getHeader(anyString())).thenReturn(null);
            Map<String, String[]> paramMap = new HashMap<>();
            paramMap.put("code", new String[]{"123"});
            when(request.getParameterMap()).thenReturn(paramMap);
            // getClass() is final and cannot be mocked; no need to stub
            when(exception.getMessage()).thenReturn("Auth failed");
            when(exception.getCause()).thenReturn(null);

            doNothing().when(response).sendRedirect(anyString());

            failureHandler.onAuthenticationFailure(request, response, exception);

            verify(response).sendRedirect("http://localhost/login?error=authentication_failed");
        }

        @Test
        @DisplayName("Should log OAuth2 error details if cause is OAuth2AuthenticationException")
        void shouldLogOAuth2ErrorDetails() throws IOException {
            OAuth2AuthenticationException oauth2Exception = mock(OAuth2AuthenticationException.class);
            when(oauth2Exception.getError()).thenReturn(
                new org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "Token expired", null)
            );
            when(exception.getCause()).thenReturn(oauth2Exception);
            when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/oauth2/callback"));
            when(request.getScheme()).thenReturn("https");
            when(request.getServerName()).thenReturn("localhost");
            when(request.getServerPort()).thenReturn(8080);
            when(request.getRequestURI()).thenReturn("/api/oauth2/callback");
            when(request.getQueryString()).thenReturn("code=123");
            when(request.getHeader(anyString())).thenReturn(null);
            when(request.getParameterMap()).thenReturn(new HashMap<>());
            // getClass() is final and cannot be mocked; no need to stub
            when(exception.getMessage()).thenReturn("OAuth2 error");

            doNothing().when(response).sendRedirect(anyString());

            failureHandler.onAuthenticationFailure(request, response, exception);

            verify(response).sendRedirect("http://localhost/login?error=authentication_failed");
        }

        @Test
        @DisplayName("Should handle empty parameter map and missing headers gracefully")
        void shouldHandleEmptyParamsAndHeaders() throws IOException {
            when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/oauth2/callback"));
            when(request.getScheme()).thenReturn("https");
            when(request.getServerName()).thenReturn("localhost");
            when(request.getServerPort()).thenReturn(8080);
            when(request.getRequestURI()).thenReturn("/api/oauth2/callback");
            when(request.getQueryString()).thenReturn(null);
            when(request.getHeader(anyString())).thenReturn(null);
            when(request.getParameterMap()).thenReturn(new HashMap<>());
            // getClass() is final and cannot be mocked; no need to stub
            when(exception.getMessage()).thenReturn("No params");
            when(exception.getCause()).thenReturn(null);

            doNothing().when(response).sendRedirect(anyString());

            failureHandler.onAuthenticationFailure(request, response, exception);

            verify(response).sendRedirect("http://localhost/login?error=authentication_failed");
        }
    }
}
