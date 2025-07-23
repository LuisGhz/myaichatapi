package dev.luisghtz.myaichat.configurationMock;

import java.util.ArrayList;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import dev.luisghtz.myaichat.auth.annotation.UserJwtData;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@TestConfiguration
public class UserJwtDataTestConfiguration {
  @Bean
  HandlerMethodArgumentResolver userJwtDataTestResolver() {
    return new HandlerMethodArgumentResolver() {
      @Override
      public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserJwtData.class);
      }

      @Override
      public Object resolveArgument(MethodParameter parameter,
          ModelAndViewContainer mavContainer,
          NativeWebRequest webRequest,
          WebDataBinderFactory binderFactory) {
        String authHeader = webRequest.getHeader("Authorization");
        String userId = authHeader != null && authHeader.startsWith("Bearer ")
            ? authHeader.substring(7)
            : "test-user-id";
        UserJwtDataDto user = new UserJwtDataDto();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        return user;
      }
    };
  }

  @Bean
  BeanPostProcessor userJwtDataResolverPostProcessor(HandlerMethodArgumentResolver userJwtDataTestResolver) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RequestMappingHandlerAdapter adapter) {
          var resolvers = new ArrayList<>(adapter.getArgumentResolvers());
          resolvers.add(0, userJwtDataTestResolver); // Add with high priority
          adapter.setArgumentResolvers(resolvers);
        }
        return bean;
      }
    };
  }
}
