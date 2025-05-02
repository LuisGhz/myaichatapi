package dev.luisghtz.myaichat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class CorsConfig {

  @Value("${server.cors.allowed-origins}")
  private String allowedOrigins;
  @Bean
  WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        log.info("CORS allowed origins: {}", allowedOrigins);
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins.split(","))
            .allowedMethods("*")
            .allowedHeaders("*");
      }
    };
  }
}
