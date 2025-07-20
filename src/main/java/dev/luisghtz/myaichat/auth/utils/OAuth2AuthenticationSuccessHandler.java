package dev.luisghtz.myaichat.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisghtz.myaichat.auth.dtos.AuthResponse;
import dev.luisghtz.myaichat.auth.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    
    @Value("${app.oauth2.success-redirect-url}")
    private String successRedirectUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
            log.error("Invalid authentication object");
            response.sendRedirect(successRedirectUrl + "?error=invalid_authentication");
            return;
        }
        
        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2Token.getPrincipal();
        
        if (oAuth2User == null) {
            log.error("OAuth2User is null");
            response.sendRedirect(successRedirectUrl + "?error=user_not_found");
            return;
        }
        
        String registrationId = oAuth2Token.getAuthorizedClientRegistrationId();
        
        log.info("OAuth2 authentication success for provider: {}", registrationId);
        log.info("User attributes: {}", oAuth2User.getAttributes());
        
        try {
            // Process authentication based on provider
            AuthResponse authResponse = processOAuth2User(registrationId, oAuth2User.getAttributes());
            
            // Redirect with token as query parameter (for development)
            // In production, consider using a different approach for security
            String redirectUrl = successRedirectUrl + "?token=" + authResponse.getToken();
            
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            response.sendRedirect(successRedirectUrl + "?error=authentication_failed");
        }
    }
    
    private AuthResponse processOAuth2User(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "github":
                return authService.authenticateWithGitHub(attributes);
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
    }
}
