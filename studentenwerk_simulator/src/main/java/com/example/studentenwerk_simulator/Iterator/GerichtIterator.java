package com.example.studentenwerk_simulator.Iterator;

import com.example.studentenwerk_simulator.gericht.Gericht;

import java.util.List;

public class GerichtIterator implements Iterator {
    private final List<Gericht> Gerichte;
    private int index;

    public GerichtIterator(List<Gericht> Gerichte) {
        this.Gerichte = Gerichte;
        this.index = 0;
    }

    @Override
    public Gericht First() {
        if (Gerichte == null || Gerichte.isEmpty()) {
            return null;
        }
        index = 0;
        return Gerichte.get(index);
    }

    @Override
    public Gericht Next() {
        if (isDone()) {
            return null;
        }
        Gericht current = Gerichte.get(index);
        index++;
        return current;
    }

    @Override
    public boolean isDone() {
        return Gerichte == null || index>= Gerichte.size();
    }

    @Override
    public Gericht currentItem() {
        if (isDone() || Gerichte.isEmpty()) {
        return null;
    }
        return Gerichte.get(index);
    }
}
