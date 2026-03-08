package com.dialgenie.auth.service;

import com.dialgenie.shared.entity.User;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginRequest;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Value("${jwt.secret:dialgenie-super-secret-key-change-in-production-1234567890qwertyuiop}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    public Map<String, Object> authenticate(User user) {
        String accessToken = generateAccessToken(user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("accessToken", accessToken);
        resp.put("tokenType", "Bearer");
        resp.put("expiresIn", jwtExpiration / 1000);
        return resp;
    }

    private String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("organizationId", user.getOrganizationId());

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
