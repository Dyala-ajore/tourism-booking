package com.youruni.tourismbooking.util;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Component
public class JwtTestUtils {
    @Value("${jwt.secret:TestSecretKeyForJWTTestingPleaseChangeInProduction123456789}")
    private String jwtSecret;
    @Value("${jwt.expiration:3600000}")  
    private long jwtExpiration;
    public String generateTestToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", 1L);
        return createToken(claims, username);
    }
    public String generateTestToken(String username) {
        return generateTestToken(username, "GUEST");
    }
    private String createToken(Map<String, Object> claims, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}