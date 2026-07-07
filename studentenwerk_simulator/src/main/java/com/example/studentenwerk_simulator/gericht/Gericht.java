package com.example.studentenwerk_simulator.gericht;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "gericht")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "typ")
public abstract class Gericht {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String beschreibung;

    private double preisStudent;
    private double preisGast;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gericht_allergene")
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    private Set<Allergen> allergene;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gericht_tags")
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Set<GerichtTag> tags;

    protected Gericht() {}

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

    public Long getId() {
        return id;
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

    public Set<Allergen> getAllergene() {
        return allergene;
    }

    public Set<GerichtTag> getTags() {
        return tags;
    }

    public abstract String getTyp();

    @Override
    public String toString() {
        return "[" + getTyp() + "] " + name + " – Student: " + preisStudent
                + "€ | Gast: " + preisGast + "€ | " + beschreibung
                + (tags.isEmpty() ? "" : " | Tags: " + tags)
                + (allergene.isEmpty() ? "" : " | Allergene: " + allergene);
    }
}
