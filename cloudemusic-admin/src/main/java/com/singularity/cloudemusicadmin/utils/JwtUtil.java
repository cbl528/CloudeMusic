package com.singularity.cloudemusicadmin.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类，负责 token 的生成与解析
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /** 生成 token，包含 userId 和 username */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    /** 解析 token 的载荷 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 token 中提取用户 ID */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /** 从 token 中提取用户名 */
    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    /** 校验 token 是否合法 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
