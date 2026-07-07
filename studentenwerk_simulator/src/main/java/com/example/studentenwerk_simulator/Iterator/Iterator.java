package com.example.studentenwerk_simulator.Iterator;

import com.example.studentenwerk_simulator.gericht.Gericht;

public interface Iterator {
    Gericht First();
    Gericht Next();
    boolean isDone();
    Gericht currentItem();
}
