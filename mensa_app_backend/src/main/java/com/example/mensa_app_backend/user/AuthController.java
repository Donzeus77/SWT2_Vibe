package com.example.mensa_app_backend.user;

import com.example.mensa_app_backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String vorname, String nachname) {}

    @PostMapping("/login")
    public ResponseEntity<AuthService.AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request.email(), request.password()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthService.AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(
                    request.email(), request.password(), request.vorname(), request.nachname()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserEntity> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
