package com.example.brightClean.service.impl;

import com.example.brightClean.domain.User;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import com.example.brightClean.util.JWTConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtService {

    private final SecretKey jwtKey;
    private final SecretKey mailKey;
    private final SecretKey registerKey;
    private final Set<String> invalidatedTokens = new HashSet<>();

    public JwtService() {
        this.jwtKey = Keys.hmacShaKeyFor(JWTConstant.SECRET.getBytes());
        this.mailKey = Keys.hmacShaKeyFor(JWTConstant.M_SECRET.getBytes());
        this.registerKey = Keys.hmacShaKeyFor(JWTConstant.REGISTER_CONSTANT.getBytes());
    }

    public String generateToken(User user, int hours, int minutes, String type) {
        String email = user.getEmail();
        SecretKey key = getKeyByType(type);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (hours * 60 * 60 * 1000) + (minutes * 60 * 1000)))
                .signWith(key)
                .compact();
    }

    public void markTokenAsUsed(String token, String type) {
        if (token == null || token.isEmpty()) {
            System.err.println("試圖標記無效的 Token，但 Token 為空！");
            return;
        }

        if ("MAIL".equals(type) || "REGISTER".equals(type)) {
            // 標記特定類型的 Token 為無效
            invalidatedTokens.add(token);
            System.out.println("已標記為無效的 " + type + " Token: " + token);
        } else {
            System.err.println("未知的 Token 類型: " + type);
        }
    }

    public boolean isTokenInvalid(String token, String type) {
        try {
            SecretKey key = getKeyByType(type);
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

            // 若 Token 已過期或已被標記為無效
            return claims.getExpiration().before(new Date()) || invalidatedTokens.contains(token);
        } catch (JwtException e) {

            return true;
        }
    }

    private SecretKey getKeyByType(String type) {
        switch (type) {
            case "MAIL":
                return mailKey;
            case "REGISTER":
                return registerKey;
            case "JWT":
                return jwtKey;
            default:
                throw new IllegalArgumentException("Invalid token type: " + type);
        }
    }

    public String getEmailFromJWT(String token, String type) {
        SecretKey key;
        switch (type) {
            case "MAIL":
                key = mailKey;
                break;
            case "REGISTER":
                key = registerKey;
                break;
            default:
                key = jwtKey;
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}