package com.pharmacy.admin.controller;

import com.pharmacy.shared.entity.User;
import com.pharmacy.shared.repository.UserRepository;
import com.pharmacy.shared.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and obtain JWT token")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}
}
