package com.example.studentenwerk_simulator.Iterator;

import com.example.studentenwerk_simulator.gericht.Gericht;

public class ArrayList implements Iterable{

    private final java.util.ArrayList<Gericht> gerichte;

    public ArrayList()  {
        this.gerichte = new java.util.ArrayList<>();
    }

    public void add(Gericht Gericht) {
        this.gerichte.add(Gericht);
    }

    public int size() {
        return this.gerichte.size();
    }

    public Gericht get(int index) {
        return this.gerichte.get(index);
    }

    @Override
    public Iterator CreateIterator(){
        return new GerichtIterator(this.gerichte);
    }
}
