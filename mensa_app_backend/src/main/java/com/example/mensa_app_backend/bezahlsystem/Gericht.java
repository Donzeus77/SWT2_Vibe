package com.example.mensa_app_backend.bezahlsystem;

// Dummy-Klasse damit Warenkorb und Bestellung keinen Fehler anzeigt
public class Gericht {
    private double preis;

    public Gericht(double preis) {
        this.preis = preis;
    }

    public double getPreis() {
        return preis;
    }
}
