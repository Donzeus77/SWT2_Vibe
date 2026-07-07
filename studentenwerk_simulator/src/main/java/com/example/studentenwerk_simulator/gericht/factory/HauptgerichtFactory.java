package com.example.studentenwerk_simulator.gericht.factory;

import com.example.studentenwerk_simulator.gericht.Allergen;
import com.example.studentenwerk_simulator.gericht.Gericht;
import com.example.studentenwerk_simulator.gericht.GerichtTag;
import com.example.studentenwerk_simulator.gericht.Hauptspeise;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class HauptgerichtFactory implements GerichtFactory {

    @Override
    public Gericht createGericht(String name, String beschreibung,
            double preisStudent, double preisGast,
            Set<Allergen> allergene, Set<GerichtTag> tags) {
        return new Hauptspeise(name, beschreibung, preisStudent, preisGast, allergene, tags);
    }
}
