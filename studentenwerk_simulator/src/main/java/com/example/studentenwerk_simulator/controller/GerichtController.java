package com.example.studentenwerk_simulator.controller;

import com.example.studentenwerk_simulator.gericht.BeilageRepository;
import com.example.studentenwerk_simulator.gericht.Gericht;
import com.example.studentenwerk_simulator.gericht.HauptspeiseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/gerichte")
public class GerichtController {

    private final HauptspeiseRepository hauptspeiseRepository;
    private final BeilageRepository beilageRepository;

    public GerichtController(HauptspeiseRepository hauptspeiseRepository, BeilageRepository beilageRepository) {
        this.hauptspeiseRepository = hauptspeiseRepository;
        this.beilageRepository = beilageRepository;
    }

    @GetMapping
    public List<Gericht> getAllGerichte() {
        List<Gericht> gerichte = new ArrayList<>();
        gerichte.addAll(hauptspeiseRepository.findAll());
        gerichte.addAll(beilageRepository.findAll());
        return gerichte;
    }
}
