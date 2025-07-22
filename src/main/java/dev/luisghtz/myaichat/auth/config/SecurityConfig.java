package dev.luisghtz.myaichat.auth.config;

import dev.luisghtz.myaichat.auth.utils.JwtAuthenticationFilter;
import dev.luisghtz.myaichat.auth.utils.OAuth2AuthenticationSuccessHandler;
import dev.luisghtz.myaichat.auth.utils.OAuth2AuthenticationFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Value("${app.base-url}")
  private String baseUrl;

  @Bean
  public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    String authorizationRequestBaseUri = isProductionEnvironment() ? "/myaichat/oauth2/authorization"
        : "/oauth2/authorization";

    DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, authorizationRequestBaseUri);

    // Add custom redirect URI resolver for production
    if (isProductionEnvironment()) {
      authorizationRequestResolver.setAuthorizationRequestCustomizer(
          authorizationRequestBuilder -> {
            String redirectUri = authorizationRequestBuilder.build().getRedirectUri();
            if (redirectUri != null && !redirectUri.contains("/myaichat/")) {
              redirectUri = redirectUri.replace("/login/oauth2/code/", "/myaichat/login/oauth2/code/");
              authorizationRequestBuilder.redirectUri(redirectUri);
            }
          });
    }

    return authorizationRequestResolver;
  }

  private boolean isProductionEnvironment() {
    return baseUrl != null && baseUrl.contains("/myaichat");
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository)
      throws Exception {
    String authorizationBaseUri = isProductionEnvironment() ? 
        "/myaichat/oauth2/authorization" : "/oauth2/authorization";
    String loginRedirectionEndpoint = isProductionEnvironment() ? 
        "/myaichat/login/oauth2/code/*" : "/login/oauth2/code/*";

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/**", "/login/**", "/oauth2/**").permitAll()
            .requestMatchers("/myaichat/auth/**", "/myaichat/login/**", "/myaichat/oauth2/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(authorization -> authorization
                .baseUri(authorizationBaseUri)
                .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository)))
            .redirectionEndpoint(redirection -> redirection
                .baseUri(loginRedirectionEndpoint))
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
