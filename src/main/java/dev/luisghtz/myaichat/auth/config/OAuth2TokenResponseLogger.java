package dev.luisghtz.myaichat.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
@SuppressWarnings("deprecation")
public class OAuth2TokenResponseLogger {

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        
        // Create a custom RestTemplate with logging
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new LoggingResponseErrorHandler());
        
        // Add request interceptor to log the outgoing request
        restTemplate.getInterceptors().add((request, body, execution) -> {
            log.info("OAuth2 Token Request:");
            log.info("URI: {}", request.getURI());
            log.info("Method: {}", request.getMethod());
            log.info("Headers: {}", request.getHeaders());
            log.info("Body: {}", new String(body, StandardCharsets.UTF_8));
            
            var response = execution.execute(request, body);
            
            log.info("OAuth2 Token Response:");
            log.info("Status: {}", response.getStatusCode());
            log.info("Headers: {}", response.getHeaders());
            
            return response;
        });
        
        client.setRestOperations(restTemplate);
        
        return new LoggingOAuth2AccessTokenResponseClient(client);
    }
    
    private static class LoggingOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        private final DefaultAuthorizationCodeTokenResponseClient delegate;
        
        public LoggingOAuth2AccessTokenResponseClient(DefaultAuthorizationCodeTokenResponseClient delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
            log.info("Making token request to GitHub...");
            log.info("Authorization code: {}", authorizationGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode());
            log.info("Client ID: {}", authorizationGrantRequest.getClientRegistration().getClientId());
            log.info("Redirect URI: {}", authorizationGrantRequest.getClientRegistration().getRedirectUri());
            
            try {
                OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationGrantRequest);
                log.info("Token response received successfully");
                log.info("Access token: {}", response.getAccessToken().getTokenValue() != null ? "***RECEIVED***" : "***NULL***");
                log.info("Token type: {}", response.getAccessToken().getTokenType());
                log.info("Scopes: {}", response.getAccessToken().getScopes());
                return response;
            } catch (Exception e) {
                log.error("Error getting token response", e);
                throw e;
            }
        }
    }
    
    private static class LoggingResponseErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
        }
        
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            log.error("HTTP Error Response - Status: {}", response.getStatusCode());
            log.error("Response Headers: {}", response.getHeaders());
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line).append("\n");
                }
                log.error("Response Body: {}", responseBody.toString());
            }
            
            // Re-throw the exception so it's handled normally
            throw new RuntimeException("OAuth2 token request failed with status: " + response.getStatusCode());
        }
    }
}
