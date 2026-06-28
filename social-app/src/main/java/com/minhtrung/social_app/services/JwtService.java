package com.minhtrung.social_app.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret.access}")
    private String accessSecret;

    @Value("${jwt.secret.refresh}")
    private String refreshSecret;

    private Key getSignKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 🔹 Access Token (short-lived)
    public String generateAccessToken(UUID userId, UUID sessionId, UUID deviceId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("sessionId", sessionId.toString())
                .claim("deviceId", deviceId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 min
                .signWith(getSignKey(accessSecret), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔹 Refresh Token (long-lived)
    public String generateRefreshToken(UUID userId, UUID sessionId, UUID deviceId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("sessionId", sessionId.toString())
                .claim("deviceId", deviceId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 days
                .signWith(getSignKey(refreshSecret), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔹 Extract userId
    public String extractUserId(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Map<String, Object> extractAll(String token, String secret) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey(secret)) 
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims;
    }
    
    // 🔹 Validate token
    public boolean isTokenValid(String token, String secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey(secret))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}