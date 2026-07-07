package com.example.mensa_app_backend.user;

import com.example.mensa_app_backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public record AuthResponse(String token, UserEntity user) {}

    public AuthResponse register(String email, String password, String vorname, String nachname) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-Mail bereits registriert");
        }
        String hash = passwordEncoder.encode(password);
        UserEntity user = UserEntity.fromRegistration(email, hash, vorname, nachname);
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getType());
        return new AuthResponse(token, user);
    }

    public AuthResponse login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Falsche E-Mail oder Passwort"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Falsche E-Mail oder Passwort");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getType());
        return new AuthResponse(token, user);
    }
}
