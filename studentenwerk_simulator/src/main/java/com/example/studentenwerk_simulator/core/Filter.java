package com.example.studentenwerk_simulator.core;

import com.example.studentenwerk_simulator.gericht.Gericht;
import com.example.studentenwerk_simulator.gericht.Allergen;
import com.example.studentenwerk_simulator.Iterator.Iterable;
import com.example.studentenwerk_simulator.Iterator.Iterator;

import java.util.ArrayList;
import java.util.List;

// Filtert Gerichte nach den Wünschen des Nutzers
public class Filter {

    // Gibt nur die Gerichte zurück, die zu den Präferenzen passen, falls kein match Display meldet
    public List<Gericht> filtere(Iterable gerichte, Praeferenzen praeferenzen) {
        List<Gericht> passende = new ArrayList<>();

       
         Iterator iterator = gerichte.CreateIterator();
        for (iterator.First(); !iterator.isDone(); iterator.Next()) {
            Gericht gericht = iterator.currentItem();
            if (passt(gericht, praeferenzen)) {
                passende.add(gericht);  
            }
        }
        return passende;
    }

    
    private boolean passt(Gericht gericht, Praeferenzen p) {
        boolean tagsPassen = gericht.getTags().containsAll(p.getGewuenschteTags());

        boolean allergeneOk = true; 
        for (Allergen allergen : p.getUnvertraeglicheAllergene()) {
            if (gericht.getAllergene().contains(allergen)) {
                allergeneOk = false;  
            }
        }

        return tagsPassen && allergeneOk;
    }
}