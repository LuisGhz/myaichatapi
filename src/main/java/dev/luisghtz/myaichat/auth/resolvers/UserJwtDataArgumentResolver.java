package dev.luisghtz.myaichat.auth.resolvers;

import org.springframework.core.MethodParameter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import dev.luisghtz.myaichat.auth.annotation.UserJwtData;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.auth.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserJwtDataArgumentResolver implements HandlerMethodArgumentResolver {
  private final JwtService jwtService;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(UserJwtData.class)
        && parameter.getParameterType().equals(UserJwtDataDto.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    String authHeader = webRequest.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return null;
    }
    String token = authHeader.replace("Bearer ", "");
    log.info("Extracted JWT token: {}", token);
    if (!jwtService.validateToken(token)) {
      log.warn("Invalid JWT token: {}", token);
      return null;
    }
    var dto = UserJwtDataDto.builder()
        .id(jwtService.getUserIdFromToken(token))
        .username(jwtService.getUsernameFromToken(token))
        .email(jwtService.getEmailFromToken(token))
        .build();
    // Add email and role extraction if needed
    return dto;
  }

}
