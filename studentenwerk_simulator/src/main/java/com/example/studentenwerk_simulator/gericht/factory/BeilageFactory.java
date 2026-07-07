package com.example.studentenwerk_simulator.gericht.factory;

import com.example.studentenwerk_simulator.gericht.Allergen;
import com.example.studentenwerk_simulator.gericht.Beilage;
import com.example.studentenwerk_simulator.gericht.Gericht;
import com.example.studentenwerk_simulator.gericht.GerichtTag;

import java.util.Set;

// Konkrete Factory für Beilagen
// Rolle im Factory Method Pattern: ConcreteCreator
public class BeilageFactory implements GerichtFactory {

    @Override
    public Gericht createGericht(String name, String beschreibung,
            double preisStudent, double preisGast,
            Set<Allergen> allergene, Set<GerichtTag> tags) {
        return new Beilage(name, beschreibung, preisStudent, preisGast, allergene, tags);
    }
}
