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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gericht gericht = (Gericht) o;
        return Double.compare(preis, gericht.preis) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(preis);
    }
}
