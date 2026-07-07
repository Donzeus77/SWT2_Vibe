package com.example.studentenwerk_simulator.gericht;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

// Abstrakte Basisklasse für alle Gerichte in der Mensa
// Rolle im Factory Method Pattern: Product
public abstract class Gericht {

    private final String name;
    private final String beschreibung;

    // Preisfelder für Studenten und externe Gäste
    private final double preisStudent;
    private final double preisGast;

    // Allergene
    private final Set<Allergen> allergene;

    // Tags (vegan etc.)
    private final Set<GerichtTag> tags;

    // Konstruktor
    protected Gericht(String name, String beschreibung,
            double preisStudent, double preisGast,
            Set<Allergen> allergene, Set<GerichtTag> tags) {
        this.name = name;
        this.beschreibung = beschreibung;
        this.preisStudent = preisStudent;
        this.preisGast = preisGast;
        this.allergene = allergene.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(allergene));
        this.tags = tags.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(tags));
    }

    public String getName() {
        return name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public double getPreisStudent() {
        return preisStudent;
    }

    public double getPreisGast() {
        return preisGast;
    }

    // Gibt alle Allergene zurück
    public Set<Allergen> getAllergene() {
        return allergene;
    }

    // Gibt alle Tags zurück
    public Set<GerichtTag> getTags() {
        return tags;
    }

    // wird von Hauptspeise und Beilage implementiert
    public abstract String getTyp();

    @Override
    public String toString() {
        return "[" + getTyp() + "] " + name + " – Student: " + preisStudent
                + "€ | Gast: " + preisGast + "€ | " + beschreibung
                + (tags.isEmpty() ? "" : " | Tags: " + tags)
                + (allergene.isEmpty() ? "" : " | Allergene: " + allergene);
    }
}
