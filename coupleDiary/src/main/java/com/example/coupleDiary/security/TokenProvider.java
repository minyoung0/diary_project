package com.example.coupleDiary.security;

import com.example.coupleDiary.service.MemberService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final MemberService memberService;
/*
    @Value("${spring.jwt.secret")
    private String secretKey;

    //private static final String ROLE_KEY = 'roles';
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; //시간

    private final MemberService memberService;

    public String generateToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);

        //권한 적용
        //claims.put(ROLE_KEY,roles)

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(expiredDate)
//                .signWith(SignatureAlgorithm.ES512, this.secretKey)//사용할 암호화 알고리즘, 비밀키
//                .compact();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // HS512로 변경
                .compact();
    }

    public String getUsername(String token){
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token){
        if(!StringUtils.hasText(token)) return false;

        var claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    //토큰이 유효한지
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        }catch(ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public Authentication getAuthentication(String jwt){
        UserDetails userDetails=this.memberService.loadUserByUsername(this.getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails,"", Collections.emptyList());
    }*/

    @Value("${jwt.secret}")
    private String secretBase64;             // Base64
    @Value("${jwt.ttl-seconds:7200}")
    private long ttlSeconds;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64)); // HS512: 64바이트 이상
    }

    public String generateToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String jwt, UserDetails user) {
        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        // 권한 안 쓰면 Collections.emptyList() 전달
    }
}
