package com.example.coupleDiary.security;

import com.example.coupleDiary.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException {
      System.out.println("[Filter] URI = " + request.getRequestURI());
      String token = resolveTokenFromRequest(request);
      System.out.println("[Filter] token(short) = " + (token == null ? null : token.substring(0, Math.min(15, token.length()))));

      if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
          String username = tokenProvider.getUsername(token);
          var user = memberService.loadUserByUsername(username);
          var auth = tokenProvider.getAuthentication(token, user);
          var context= SecurityContextHolder.createEmptyContext();
          context.setAuthentication(auth);
          SecurityContextHolder.setContext(context);
          System.out.println("[Filter] JWT OK as " + username);
      } else {
          System.out.println("[Filter] no/invalid token");
      }
      chain.doFilter(request, response);
  }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (org.springframework.util.StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            System.out.println("[Filter] Authorization header present");
            return bearer.substring(7);
        }
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                System.out.println("[Filter] cookie " + c.getName());
                if ("access_token".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

}
