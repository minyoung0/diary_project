package com.example.coupleDiary.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${spring.jwt.secret")
    private String secretKey;

    //private static final String ROLE_KEY = 'roles';
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; //시간

    public String generateToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);

        //권한 적용
        //claims.put(ROLE_KEY,roles)

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.ES512, this.secretKey)//사용할 암호화 알고리즘, 비밀키
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
}
