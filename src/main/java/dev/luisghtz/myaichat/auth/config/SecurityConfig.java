package dev.luisghtz.myaichat.auth.config;

import dev.luisghtz.myaichat.auth.utils.JwtAuthenticationFilter;
import dev.luisghtz.myaichat.auth.utils.OAuth2AuthenticationSuccessHandler;
import dev.luisghtz.myaichat.auth.utils.OAuth2AuthenticationFailureHandler;
import dev.luisghtz.myaichat.auth.utils.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.support.HttpRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Value("${app.base-url}")
  private String baseUrl;

  @Bean
  OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    String authorizationRequestBaseUri = "/oauth2/authorization";

    DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, authorizationRequestBaseUri);

    // Create a custom resolver that ensures HTTPS redirect URIs
    return new OAuth2AuthorizationRequestResolver() {
      @Override
      public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return resolveWithHttps(authorizationRequestResolver.resolve(request));
      }

      @Override
      public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return resolveWithHttps(authorizationRequestResolver.resolve(request, clientRegistrationId));
      }

      private OAuth2AuthorizationRequest resolveWithHttps(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
          return null;
        }

        String redirectUri = authorizationRequest.getRedirectUri();
        log.info("Original OAuth2 redirect URI: {}", redirectUri);

        if (redirectUri != null && redirectUri.startsWith("http://") && redirectUri.contains("apis.luisghtz.dev")) {
          String httpsRedirectUri = redirectUri.replace("http://", "https://");
          log.info("Converting HTTP to HTTPS redirect URI: {} -> {}", redirectUri, httpsRedirectUri);

          return OAuth2AuthorizationRequest
              .from(authorizationRequest)
              .redirectUri(httpsRedirectUri)
              .build();
        }

        return authorizationRequest;
      }
    };
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository)
      throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false))
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(customAuthenticationEntryPoint))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/**", "/login/**", "/oauth2/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(authorization -> authorization
                .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository)))
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler)
            .redirectionEndpoint(endpoint -> endpoint
                .baseUri("/login/oauth2/code/*")))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
    FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new ForwardedHeaderFilter());
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }

  @Bean
  RestTemplate oauth2RestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add((request, body, execution) -> {
      URI uri = request.getURI();
      if (uri.getScheme().equals("http") && uri.getHost().equals("apis.luisghtz.dev")) {
        try {
          URI httpsUri = new URI("https", uri.getUserInfo(), uri.getHost(),
              uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
          return execution.execute(
              new HttpRequestWrapper(request) {
                @Override
                public URI getURI() {
                  return httpsUri;
                }
              }, body);
        } catch (URISyntaxException e) {
          // Log error
        }
      }
      return execution.execute(request, body);
    });
    return restTemplate;
  }
}
