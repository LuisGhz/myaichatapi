package dev.luisghtz.myaichat.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        log.debug("Authentication failed for request: {} {}", request.getMethod(), request.getRequestURI());
        
        // Check if this is an API request (starts with /api/)
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/")) {
            // Return 401 for API requests
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Authentication required");
            errorResponse.put("status", 401);
            errorResponse.put("path", requestURI);
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } else {
            // For non-API requests (like OAuth2 flows), use default behavior
            response.sendRedirect("/oauth2/authorization/github");
        }
    }
}