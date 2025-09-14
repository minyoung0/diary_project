package com.example.coupleDiary.security;
import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGen {
    public static void main(String[] args) {
        byte[] key = new byte[64];  // 64바이트 이상
        new SecureRandom().nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Generated JWT Secret (Base64):");
        System.out.println(base64Key);
    }
}
