package com.example.studentenwerk_simulator.core;

import com.example.studentenwerk_simulator.gericht.Allergen;
import com.example.studentenwerk_simulator.gericht.GerichtTag;

import java.util.Set;

public class Praeferenzen {

    private Set<GerichtTag> gewuenschteTags;          
    private Set<Allergen> unvertraeglicheAllergene;  

    public Praeferenzen(Set<GerichtTag> gewuenschteTags,
                        Set<Allergen> unvertraeglicheAllergene) {
        this.gewuenschteTags = gewuenschteTags;
        this.unvertraeglicheAllergene = unvertraeglicheAllergene;
    }

    public Set<GerichtTag> getGewuenschteTags() {
        return gewuenschteTags;
    }

    public Set<Allergen> getUnvertraeglicheAllergene() {
        return unvertraeglicheAllergene;
    }
}