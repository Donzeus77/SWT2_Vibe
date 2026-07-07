package com.example.studentenwerk_simulator.gericht;

import java.util.Set;

// Beilage als Unterklasse von Gericht
// Rolle im Pattern: ConcreteProduct
public class Beilage extends Gericht {

    // Konstruktor leitet alles an die abstrakte Klasse weiter
    public Beilage(String name, String beschreibung,
            double preisStudent, double preisGast,
            Set<Allergen> allergene, Set<GerichtTag> tags) {
        super(name, beschreibung, preisStudent, preisGast, allergene, tags);
    }

    @Override
    public String getTyp() {
        return "Beilage";
    }
}
