package com.example.prompthubbackend.util;

import com.example.prompthubbackend.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final String secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public Key getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Role role){
        return Jwts.builder()
                .setSubject(username)
                .claim("role",role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }
    public String extractRole(String token){
        return extractAllClaims(token).get("role").toString();
    }
    public boolean isTokenValid(String token){
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
