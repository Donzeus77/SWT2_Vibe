package com.example.studentenwerk_simulator.gericht;

import java.util.Set;

// Hauptspeise als konkrete Unterklasse von Gericht
// Rolle im Pattern: ConcreteProduct
public class Hauptspeise extends Gericht {

    // Konstruktor leitet alles an die abstrakte Klasse weiter
    public Hauptspeise(String name, String beschreibung,
            double preisStudent, double preisGast,
            Set<Allergen> allergene, Set<GerichtTag> tags) {
        super(name, beschreibung, preisStudent, preisGast, allergene, tags);
    }

    @Override
    public String getTyp() {
        return "Hauptspeise";
    }
}
