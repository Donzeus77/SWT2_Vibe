package com.example.mensa_app_backend.bezahlsystem;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Warenkorb_Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long gerichtId;
    private String name;
    private double preis;
    private int anzahl;
    @ManyToOne private Warenkorb warenkorb;
    @ManyToOne private Bestellung bestellung;
    protected Warenkorb_Item() {}
    public Warenkorb_Item(Warenkorb warenkorb, Long gerichtId, String name, double preis, int anzahl) { this.warenkorb = warenkorb; this.gerichtId = gerichtId; this.name = name; this.preis = preis; this.anzahl = anzahl; }
    public Warenkorb_Item(Bestellung bestellung, Warenkorb_Item quelle) { this.bestellung = bestellung; this.gerichtId = quelle.gerichtId; this.name = quelle.name; this.preis = quelle.preis; this.anzahl = quelle.anzahl; }
    public Long getGerichtId() { return gerichtId; } public String getName() { return name; } public double getPreis() { return preis; } public int getAnzahl() { return anzahl; }
    public void addAnzahl(int menge) { anzahl += menge; }
    public double berechnePreis() { return anzahl * preis; }
}
