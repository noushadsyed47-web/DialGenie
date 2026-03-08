package com.dialgenie.auth.controller;

import com.dialgenie.auth.dto.LoginRequest;
import com.dialgenie.auth.service.AuthService;
import com.dialgenie.shared.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        String sql = "SELECT id, email, password_hash, first_name, last_name, organization_id FROM users WHERE email = ?";
        Map<String, Object> user = null;
        try {
            user = jdbcTemplate.queryForMap(sql, req.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials", null));
        }

        String storedHash = (String) user.get("password_hash");
        if (!passwordEncoder.matches(req.getPassword(), storedHash)) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials", null));
        }

        // Build minimal User object for token generation
        com.dialgenie.shared.entity.User u = new com.dialgenie.shared.entity.User();
        u.setId((String) user.get("id"));
        u.setEmail((String) user.get("email"));
        u.setFirstName((String) user.get("first_name"));
        u.setLastName((String) user.get("last_name"));
        u.setOrganizationId((String) user.get("organization_id"));

        var resp = authService.authenticate(u);
        return ResponseEntity.ok(ApiResponse.success(resp, "Login successful"));
    }
}
