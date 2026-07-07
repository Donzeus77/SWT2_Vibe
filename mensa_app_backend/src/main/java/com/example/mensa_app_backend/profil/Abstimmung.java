package com.example.mensa_app_backend.profil;

//import com.example.studentenwerk_simulator.gericht;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;

public class Abstimmung {
    private ArrayList<Hauptspeise> gerichte;
    private ArrayList<Integer> anzStimmen;
    private LocalDateTime abstimmungsende;

    public Abstimmung(LocalDateTime bis_wann) {
        abstimmungsende = bis_wann;
        gerichte = new ArrayList<>();
        anzStimmen = new ArrayList<>();
    }

    public boolean istAktiv() {
        return abstimmungsende.isAfter(LocalDateTime.now());
    }

    public int getIndexOf(Hauptspeise gericht) {
        int index = -1;
        for(int i=0; i < gerichte.size(); i++) {
            if(gerichte.get(i).equals(gericht)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean addGericht(Hauptspeise gericht) {
        if(getIndexOf(gericht) == -1) { // Gerichte nicht doppelt eintragen
            gerichte.add(gericht);
            anzStimmen.add(0);
            return true;
        }
        return false;
    }

    public boolean removeGericht(Hauptspeise gericht) {
        int index = getIndexOf(gericht);
        if(index != -1) {
            gerichte.remove(index);
            anzStimmen.remove(index);
            return true;
        }
        return false;
    }

    public boolean abstimmen(Hauptspeise gericht) {
        int index = getIndexOf(gericht);
        if(index != -1) {
            anzStimmen.set(index, anzStimmen.get(index)+1);
            return true;
        }
        return false;
    }

    public int getAnzStimmenOf(Hauptspeise gericht) {
        int index = getIndexOf(gericht);
        return index != -1 ? anzStimmen.get(index) : 0;
    }

    public int getAnzStimmenGesamt() {
        int summe = 0;
        for(int i=0; i < gerichte.size(); i++) {
            summe += getAnzStimmenOf(gerichte.get(i));
        }
        return summe;
    }

    public Hauptspeise[] getGewinner() {
        if(!istAktiv() && gerichte.size() > 0) {
            int gewinnerIndex = 0;
            LinkedList<Hauptspeise> gewinner = new LinkedList<>();
            for(int i=0; i < anzStimmen.size(); i++) {
                if(anzStimmen.get(i) > anzStimmen.get(gewinnerIndex)) {
                    gewinnerIndex = i;
                    gewinner = new LinkedList<>();
                    gewinner.add(gerichte.get(i));
                } else if(anzStimmen.get(i) == anzStimmen.get(gewinnerIndex)) {
                    gewinner.add(gerichte.get(i));
                }
            }
            Hauptspeise[] gewinnerArray = new Hauptspeise[gewinner.size()];
            for(int i=0; i < gewinner.size(); i++) {
                gewinnerArray[i] = gewinner.get(i);
            }
            return gewinnerArray;
        }
        return new Hauptspeise[0];
    }

}
