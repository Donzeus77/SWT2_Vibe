package com.example.studentenwerk_simulator.controller;

import com.example.studentenwerk_simulator.mensa.Mensa;
import com.example.studentenwerk_simulator.mensa.MensaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mensen")
public class MensaController {

    private final MensaRepository mensaRepository;

    public MensaController(MensaRepository mensaRepository) {
        this.mensaRepository = mensaRepository;
    }

    @GetMapping
    public List<Mensa> getAllMensen() {
        return mensaRepository.findAll();
    }
}
