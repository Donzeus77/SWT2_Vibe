package com.example.mensa_app_backend.mensa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensen")
public class MensaController {

    private final MensaCache mensaCache;

    public MensaController(MensaCache mensaCache) {
        this.mensaCache = mensaCache;
    }

    @GetMapping
    public List<Map<String, Object>> getAllMensen() {
        return mensaCache.getAll();
    }
}
