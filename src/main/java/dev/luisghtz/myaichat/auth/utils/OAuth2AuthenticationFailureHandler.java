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
        
        log.error("OAuth2 authentication failure", exception);
        var loginpath = successRedirectUrl.replace("success", "login");
        // Redirect to the same base URL as success, but with error parameter
        String failureRedirectUrl = loginpath + "?error=authentication_failed";
        response.sendRedirect(failureRedirectUrl);
    }
}
