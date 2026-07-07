package com.example.mensa_app_backend.preference;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profil/preferences")
public class PreferenceController {

    private final PreferenceRepository repository;

    public PreferenceController(PreferenceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getPreferences(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Preference pref = repository.findByUserId(userId).orElse(null);
        if (pref == null) {
            return ResponseEntity.ok(Map.of("dietary", List.of(), "allergens", List.of()));
        }
        return ResponseEntity.ok(Map.of("dietary", pref.getDietary(), "allergens", pref.getAllergens()));
    }

    public record PreferenceUpdate(List<String> dietary, List<String> allergens) {}

    @PutMapping
    public ResponseEntity<Void> updatePreferences(Authentication auth, @RequestBody PreferenceUpdate update) {
        Long userId = (Long) auth.getPrincipal();
        Preference pref = repository.findByUserId(userId).orElse(new Preference(userId));
        pref.setDietary(update.dietary() != null ? update.dietary() : List.of());
        pref.setAllergens(update.allergens() != null ? update.allergens() : List.of());
        repository.save(pref);
        return ResponseEntity.ok().build();
    }
}
