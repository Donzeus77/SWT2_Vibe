package com.example.mensa_app_backend.preference;

import com.example.mensa_app_backend.profil.Profil;
import com.example.mensa_app_backend.profil.ProfilRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/profil/preferences")
public class PreferenceController {
    private final PreferenceRepository repository; private final ProfilRepository profilRepository;
    public PreferenceController(PreferenceRepository repository, ProfilRepository profilRepository) { this.repository = repository; this.profilRepository = profilRepository; }
    public record PreferenceUpdate(List<String> dietary, List<String> allergens) {}
    @GetMapping public ResponseEntity<Map<String, List<String>>> get(@RequestParam String email) { Profil profil = profilRepository.findByEmail(email).orElse(null); if (profil == null) return ResponseEntity.notFound().build(); Preference p = repository.findByUserId(profil.getId()).orElse(null); return ResponseEntity.ok(Map.of("dietary", p == null ? List.of() : p.getDietary(), "allergens", p == null ? List.of() : p.getAllergens())); }
    @PutMapping public ResponseEntity<Void> update(@RequestParam String email, @RequestBody PreferenceUpdate update) { Profil profil = profilRepository.findByEmail(email).orElse(null); if (profil == null) return ResponseEntity.notFound().build(); Preference p = repository.findByUserId(profil.getId()).orElse(new Preference(profil.getId())); p.setDietary(update.dietary() == null ? List.of() : update.dietary()); p.setAllergens(update.allergens() == null ? List.of() : update.allergens()); repository.save(p); return ResponseEntity.ok().build(); }
}
