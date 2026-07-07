package com.example.studentenwerk_simulator.gericht.factory;

import com.example.studentenwerk_simulator.gericht.Allergen;
import com.example.studentenwerk_simulator.gericht.Gericht;
import com.example.studentenwerk_simulator.gericht.GerichtTag;

import java.util.Set;

// Factory Interface
// Rolle im Factory Method Pattern: Creator
public interface GerichtFactory {

    // Die eigentliche Factory-Methode
    Gericht createGericht(String name, String beschreibung,
            double preisStudent, double preisGast,
            Set<Allergen> allergene, Set<GerichtTag> tags);
}
