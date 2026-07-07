package com.example.mensa_app_backend.bezahlsystem;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class Bestellung {
    private static int anzahlBestellungen = 0;
    private long bestellnr;
    private LocalDateTime bestelldatum;
    private LocalDateTime abholdatum;
    private boolean bezahlt = false;
    private boolean abgeholt = false;
    private boolean storniert = false;
    private LinkedList<Warenkorb_Item> artikel;

    public Bestellung(Warenkorb warenkorb, LocalDateTime abholdatum) {
        bestellnr = ++anzahlBestellungen;
        artikel = warenkorb.getArtikel();
        this.abholdatum = abholdatum;
        bestelldatum = LocalDateTime.now();
    }

    public long getBestellnr() {
        return bestellnr;
    }

    public String getBestelldatum() {
        return displayDate(bestelldatum) + displayTime(bestelldatum);
    }

    public String getAbholdatum() {
        return displayDate(abholdatum) + displayTime(abholdatum);
    }

    private String displayDate(LocalDateTime date) {
        return date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear(); 
    }

    private String displayTime(LocalDateTime time) {
        return time.getHour() + ":" + time.getMinute();
    }

    public boolean istBezahlt() {
        return bezahlt;
    }

    public boolean istAbgeholt() {
        return abgeholt;
    }

    public boolean istStorniert() {
        return storniert;
    }

    public void bezahlen() {
        bezahlt = true;
    }

    public void abholen() {
        abgeholt = true;
    }

    public void stornieren() {
        storniert = true;
    }

}
