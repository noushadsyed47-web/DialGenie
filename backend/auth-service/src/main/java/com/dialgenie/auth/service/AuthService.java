package com.dialgenie.auth.service;

import com.dialgenie.shared.dto.UserDTO;
import com.dialgenie.shared.entity.User;
import com.dialgenie.shared.exception.UnauthorizedException;
import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.dto.LoginResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${jwt.secret:dialgenie-super-secret-key-change-in-production-1234567890qwertyuiop}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration;

    public LoginResponse authenticate(User user) {
        logger.info("Authenticating user: {}", user.getEmail());
        
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration / 1000)
                .tokenType("Bearer")
                .build();
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("organizationId", user.getOrganizationId());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        return createToken(claims, user.getEmail(), jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "refresh");

        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, "userId", String.class);
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getOrganizationIdFromToken(String token) {
        return getClaimFromToken(token, "organizationId", String.class);
    }

    public <T> T getClaimFromToken(String token, String claimName, Class<T> claimType) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(claimName, claimType);
    }

    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.error("JWT token expired: {}", e.getMessage());
            throw new UnauthorizedException("Token has expired");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT token");
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            throw new UnauthorizedException("JWT signature validation failed");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new UnauthorizedException("JWT claims string is empty");
        }
    }

    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
