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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warenkorb_Item that = (Warenkorb_Item) o;
        return gericht != null && gericht.equals(that.gericht);
    }

    @Override
    public int hashCode() {
        return gericht != null ? gericht.hashCode() : 0;
    }
}
