package com.example.studentenwerk_simulator.mensa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mensa")
public class Mensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String campus;
    private String adresse;
    private String oeffnungszeiten;
    private String auslastung;

    protected Mensa() {}

    public Mensa(String name, String campus, String adresse, String oeffnungszeiten, String auslastung) {
        this.name = name;
        this.campus = campus;
        this.adresse = adresse;
        this.oeffnungszeiten = oeffnungszeiten;
        this.auslastung = auslastung;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCampus() { return campus; }
    public String getAdresse() { return adresse; }
    public String getOeffnungszeiten() { return oeffnungszeiten; }
    public String getAuslastung() { return auslastung; }
}
