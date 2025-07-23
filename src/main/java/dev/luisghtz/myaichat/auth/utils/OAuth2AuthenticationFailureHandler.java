package dev.luisghtz.myaichat.auth.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Value("${app.oauth2.success-redirect-url}")
    private String successRedirectUrl;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException {
        
        log.error("OAuth2 authentication failure for request: {}", request.getRequestURL());
        log.error("Request scheme: {}", request.getScheme());
        log.error("Request server name: {}", request.getServerName());
        log.error("Request server port: {}", request.getServerPort());
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Request query string: {}", request.getQueryString());
        log.error("X-Forwarded-Proto header: {}", request.getHeader("X-Forwarded-Proto"));
        log.error("X-Forwarded-Host header: {}", request.getHeader("X-Forwarded-Host"));
        log.error("X-Forwarded-For header: {}", request.getHeader("X-Forwarded-For"));
        
        // Log all parameters individually
        log.error("Request parameters count: {}", request.getParameterMap().size());
        request.getParameterMap().forEach((key, values) -> {
            log.error("Parameter '{}': {}", key, String.join(", ", values));
        });
        
        log.error("Exception type: {}", exception.getClass().getSimpleName());
        log.error("Exception message: {}", exception.getMessage());
        log.error("OAuth2 authentication failure", exception);
        
        // Extract more details if it's an OAuth2AuthenticationException
        if (exception.getCause() instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
            org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth2Exception = 
                (org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception.getCause();
            log.error("OAuth2 error code: {}", oauth2Exception.getError().getErrorCode());
            log.error("OAuth2 error description: {}", oauth2Exception.getError().getDescription());
        }
        
        var loginpath = successRedirectUrl.replace("success", "login");
        // Redirect to the same base URL as success, but with error parameter
        String failureRedirectUrl = loginpath + "?error=authentication_failed";
        
        log.info("Redirecting to failure URL: {}", failureRedirectUrl);
        response.sendRedirect(failureRedirectUrl);
    }
}
