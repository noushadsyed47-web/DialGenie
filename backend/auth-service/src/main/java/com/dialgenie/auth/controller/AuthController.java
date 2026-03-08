package com.dialgenie.auth.controller;

import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.service.AuthService;
import com.dialgenie.shared.dto.ApiResponse;
import com.dialgenie.shared.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getEmail());
        
        // TODO: Fetch user from database and validate credentials
        // For now, this is a placeholder
        
        return ResponseEntity.ok(ApiResponse.success(null, "Login endpoint placeholder"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        logger.info("Token refresh requested");
        
        // TODO: Implement token refresh logic
        
        return ResponseEntity.ok(ApiResponse.success(null, "Token refresh placeholder"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid, "Token validation result"));
    }
}
