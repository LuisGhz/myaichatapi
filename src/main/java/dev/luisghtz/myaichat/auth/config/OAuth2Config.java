package dev.luisghtz.myaichat.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OAuth2Config {

  @Value("${spring.security.oauth2.client.registration.github.client-id:placeholder-client-id}")
  private String githubClientId;

  @Value("${spring.security.oauth2.client.registration.github.client-secret:placeholder-client-secret}")
  private String githubClientSecret;

  @Value("${app.base-url}")
  private String baseUrl;

  // Uncommented since BASIC method works in local environment
  // This configuration takes precedence over the YAML configuration
  @Bean
  ClientRegistrationRepository clientRegistrationRepository() {
    log.info("Creating ClientRegistrationRepository with custom configuration");
    return new InMemoryClientRegistrationRepository(getGithubClientRegistration());
  }

  private ClientRegistration getGithubClientRegistration() {
    // Ensure HTTPS is used for production redirect URI
    String httpsBaseUrl = baseUrl.startsWith("http://") ? baseUrl.replace("http://", "https://") : baseUrl;
    String redirectUri = httpsBaseUrl + "/login/oauth2/code/github";

    log.info("GitHub OAuth2 Configuration:");
    log.info("Client ID: {}", githubClientId);
    log.info("Base URL: {}", baseUrl);
    log.info("HTTPS Base URL: {}", httpsBaseUrl);
    log.info("Redirect URI: {}", redirectUri);
    log.info("Client Secret: {}",
        githubClientSecret != null && !githubClientSecret.isEmpty() ? "***SET***" : "***NOT SET***");

    ClientRegistration registration = ClientRegistration.withRegistrationId("github")
        .clientId(githubClientId)
        .clientSecret(githubClientSecret)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri(redirectUri)
        .scope("user:email", "read:user")
        .authorizationUri("https://github.com/login/oauth/authorize")
        .tokenUri("https://github.com/login/oauth/access_token")
        .userInfoUri("https://api.github.com/user")
        .userNameAttributeName("id")
        .clientName("GitHub")
        .build();
        
    log.info("Final ClientRegistration redirect URI: {}", registration.getRedirectUri());
    
    return registration;
  }
}
