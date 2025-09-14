package com.example.coupleDiary.config;

import com.example.coupleDiary.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/loginPage", "/joinPage",
                                "/signup", "/signin", "/error",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())   // ✅ 폼로그인 비활성화(세션X)
                .logout(lo -> lo.logoutUrl("/logout").logoutSuccessUrl("/"));

        // ✅ JWT 필터 등록
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
