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
  public ClientRegistrationRepository clientRegistrationRepository() {
    log.info("Creating ClientRegistrationRepository with custom configuration");
    return new InMemoryClientRegistrationRepository(getGithubClientRegistration());
  }

  private ClientRegistration getGithubClientRegistration() {
    String baseDomain = baseUrl;
    if (baseDomain.endsWith("/myaichat")) {
      baseDomain = baseDomain.substring(0, baseDomain.length() - 9); // Remove "/myaichat"
    }

    // Determine if we're in production (based on baseUrl containing /myaichat)
    boolean isProduction = baseUrl != null && baseUrl.contains("/myaichat");
    String redirectUri = isProduction ? 
        baseDomain + "/myaichat/login/oauth2/code/github" : 
        baseDomain + "/login/oauth2/code/github";
    
    log.info("GitHub OAuth2 Configuration:");
    log.info("Client ID: {}", githubClientId);
    log.info("Base URL: {}", baseUrl);
    log.info("Environment: {}", isProduction ? "Production" : "Development");
    log.info("Redirect URI: {}", redirectUri);
    log.info("Client Secret: {}",
        githubClientSecret != null && !githubClientSecret.isEmpty() ? "***SET***" : "***NOT SET***");

    return ClientRegistration.withRegistrationId("github")
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
  }
}
