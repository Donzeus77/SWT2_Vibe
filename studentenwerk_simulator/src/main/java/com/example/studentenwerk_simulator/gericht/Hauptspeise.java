package com.example.studentenwerk_simulator.gericht;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue("HAUPTSPEISE")
public class Hauptspeise extends Gericht {

    protected Hauptspeise() {}

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
