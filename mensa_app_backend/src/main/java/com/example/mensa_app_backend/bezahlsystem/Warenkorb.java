package com.example.mensa_app_backend.bezahlsystem;

import com.example.mensa_app_backend.profil.Hauptspeise;
import com.example.mensa_app_backend.profil.Profil;
import java.util.LinkedList;

public class Warenkorb {
    private Profil besitzer;
    private LinkedList<Warenkorb_Item> artikel;

    public Warenkorb(Profil profil) {
        besitzer = profil;
        artikel = new LinkedList<>();
    }

    public LinkedList<Warenkorb_Item> getArtikel() {
        return artikel;
    }

    public int getIndexOf(Warenkorb_Item item) {
        int index = -1;
        for(int i=0; i < artikel.size(); i++) {
            if(artikel.get(i).equals(item)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void in_den_Warenkorb(Warenkorb_Item item) {
        int index = getIndexOf(item);
        if(index < 0) {
            artikel.add(item);
        } else {
            artikel.get(index).increment();
        }
    }

    public double getWarenwert() {
        double sum = 0;
        for(Warenkorb_Item i: artikel) {
            sum += i.berechnePreis();
        }
        return sum;
    }

    public void leeren() {
        artikel = new LinkedList<>();
    }
}
