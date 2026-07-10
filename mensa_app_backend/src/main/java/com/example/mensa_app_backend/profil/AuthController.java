package com.example.mensa_app_backend.profil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final ProfilRepository repository;
    public AuthController(ProfilRepository repository) { this.repository = repository; }
    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password) {}
    public record ProfilResponse(Long id, String email, String vorname, String nachname, String type) {
        static ProfilResponse from(Profil p) { return new ProfilResponse(p.getId(), p.getEmail(), p.getVorname(), p.getNachname(), p.getStatus()); }
    }
    @PostMapping("/register")
    public ResponseEntity<ProfilResponse> register(@RequestBody RegisterRequest request) {
        if (request.email() == null || request.password() == null || repository.findByEmail(request.email()).isPresent()) return ResponseEntity.badRequest().build();
        Profil profil = new Profil(request.email(), request.password());
        if ("ungueltig".equals(profil.getStatus())) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ProfilResponse.from(repository.save(profil)));
    }
    @PostMapping("/login")
    public ResponseEntity<ProfilResponse> login(@RequestBody LoginRequest request) {
        return repository.findByEmail(request.email()).filter(p -> p.getPasswort().equals(request.password())).map(p -> ResponseEntity.ok(ProfilResponse.from(p))).orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
