package com.example.mensa_app_backend.bezahlsystem;

public class Warenkorb_Item {
    private Gericht gericht;
    private int anzahl;

    public Warenkorb_Item(Gericht gericht) {
        this.gericht = gericht;
        anzahl = 1;
    }

    public Warenkorb_Item(Gericht gericht, int anz) {
        this.gericht = gericht;
        anzahl = anz;
    }

    public Gericht getGericht() {
        return gericht;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void increment() {
        anzahl++;
    }

    public double berechnePreis() {
        return anzahl * gericht.getPreis();
    }

    // TODO: equals überschreiben
}
