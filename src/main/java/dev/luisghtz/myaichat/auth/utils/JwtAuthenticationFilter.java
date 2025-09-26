package dev.luisghtz.myaichat.auth.utils;

import dev.luisghtz.myaichat.auth.entities.User;
import dev.luisghtz.myaichat.auth.services.JwtService;
import dev.luisghtz.myaichat.auth.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserService userService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    log.info("Token from filter: {}", authHeader);
    try {
      String token = authHeader.substring(7);

      boolean valid = jwtService.validateToken(token);

      if (valid && SecurityContextHolder.getContext().getAuthentication() == null) {
        String userId = jwtService.getUserIdFromToken(token);

        if (userId != null) {
          User user = userService.findById(userId).orElse(null);

          if (user != null && !user.getLocked() && !user.getDisabled()) {
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().name()));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
          } else {
            log.debug("User not found, locked, or disabled for token");
            SecurityContextHolder.clearContext();
          }
        } else {
          log.debug("No user ID found in token");
          SecurityContextHolder.clearContext();
        }
      } else if (!valid) {
        log.debug("Invalid JWT token");
        SecurityContextHolder.clearContext();
      }
    } catch (Exception e) {
      log.error("Error processing JWT token", e);
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
