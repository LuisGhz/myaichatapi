package dev.luisghtz.myaichat.auth.resolvers;

import dev.luisghtz.myaichat.auth.annotation.UserJwtData;
import dev.luisghtz.myaichat.auth.dtos.UserJwtDataDto;
import dev.luisghtz.myaichat.auth.services.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
// ...existing code...
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.bind.support.WebDataBinderFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserJwtDataArgumentResolver Unit Tests")
class UserJwtDataArgumentResolverTest {
    @Mock
    JwtService jwtService;
    @Mock
    MethodParameter methodParameter;
    @Mock
    NativeWebRequest webRequest;
    @Mock
    ModelAndViewContainer mavContainer;
    @Mock
    WebDataBinderFactory binderFactory;

    @InjectMocks
    UserJwtDataArgumentResolver resolver;

    @Nested
    @DisplayName("supportsParameter method")
    class SupportsParameterTests {
        @Test
        @DisplayName("Should return true when parameter has @UserJwtData and type UserJwtDataDto")
        void shouldReturnTrueForValidParameter() {
            when(methodParameter.hasParameterAnnotation(UserJwtData.class)).thenReturn(true);
            when(methodParameter.getParameterType()).thenReturn((Class) UserJwtDataDto.class);
            assertTrue(resolver.supportsParameter(methodParameter));
        }

        @Test
        @DisplayName("Should return false when parameter type is not UserJwtDataDto")
        void shouldReturnFalseForWrongType() {
            when(methodParameter.hasParameterAnnotation(UserJwtData.class)).thenReturn(true);
            when(methodParameter.getParameterType()).thenReturn((Class) String.class);
            assertFalse(resolver.supportsParameter(methodParameter));
        }
    }

    @Nested
    @DisplayName("resolveArgument method")
    class ResolveArgumentTests {
        @Test
        @DisplayName("Should return null if Authorization header is missing")
        void shouldReturnNullIfAuthHeaderMissing() {
            when(webRequest.getHeader("Authorization")).thenReturn(null);
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null if Authorization header does not start with Bearer")
        void shouldReturnNullIfAuthHeaderInvalid() {
            when(webRequest.getHeader("Authorization")).thenReturn("Basic token");
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null if JWT token is invalid")
        void shouldReturnNullIfJwtInvalid() {
            when(webRequest.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
            when(jwtService.validateToken("invalidtoken")).thenReturn(false);
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return UserJwtDataDto if JWT token is valid")
        void shouldReturnDtoIfJwtValid() {
            String token = "validtoken";
            when(webRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.getUserIdFromToken(token)).thenReturn("123");
            when(jwtService.getUsernameFromToken(token)).thenReturn("testuser");
            when(jwtService.getEmailFromToken(token)).thenReturn("test@example.com");

            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
            assertNotNull(result);
            assertTrue(result instanceof UserJwtDataDto);
            UserJwtDataDto dto = (UserJwtDataDto) result;
            assertEquals("123", dto.getId());
            assertEquals("testuser", dto.getUsername());
            assertEquals("test@example.com", dto.getEmail());
        }
    }
}
