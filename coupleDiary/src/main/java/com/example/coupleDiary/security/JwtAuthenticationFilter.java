package com.example.coupleDiary.security;

import com.example.coupleDiary.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER="Authorization";
    public static final String TOKEN_PREFIX="Bearer ";

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    /* @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         String token =this.resolveTokenFromRequest(request);
         System.out.println(token);
         if(StringUtils.hasText(token)&&this.tokenProvider.validateToken(token)){
             //토큰이 유효성 검증
             Authentication auth=this.tokenProvider.getAuthentication(token);
             SecurityContextHolder.getContext().setAuthentication(auth);
         }

         filterChain.doFilter(request,response);
     }

     private String resolveTokenFromRequest(HttpServletRequest request){
         String token=request.getHeader(TOKEN_HEADER);
         // 1. Authorization 헤더 먼저 확인
         if(!ObjectUtils.isEmpty(token)&&token.startsWith(TOKEN_PREFIX)){
             return token.substring(TOKEN_PREFIX.length());
         }
         // 2. 쿠키에서도 확인
         if (request.getCookies() != null) {
             for (var cookie : request.getCookies()) {
                 if ("access_token".equals(cookie.getName())) {
                     return cookie.getValue();
                 }
             }
         }
         return null;
     }*/
   @Override
   protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

       String token = this.resolveTokenFromRequest(request);
       System.out.println("[Filter] token: "+token);
       if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
           String username = tokenProvider.getUsername(token);
           UserDetails user = memberService.loadUserByUsername(username); // 필터에 주입 필요
           Authentication auth = tokenProvider.getAuthentication(token, user);
           SecurityContextHolder.getContext().setAuthentication(auth);
           log.debug("JWT 인증 성공: {}", auth.getName());
       } else {
           log.debug("JWT 인증 실패 또는 토큰 없음");
       }

       filterChain.doFilter(request, response);
   }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        // 1. Authorization 헤더 확인
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }

        // 2. 쿠키 확인
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
