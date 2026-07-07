package com.example.mensa_app_backend.profil;

import com.example.mensa_app_backend.bezahlsystem.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Profil {
    private static int anzahlNutzer = 0;
    // Nutzerdaten
    private long id;
    private String email;
    private String passwort;
    private String vorname;
    private String nachname;
    private String status;
    // Bestellen und Bezahlen
    private Warenkorb warenkorb;
    private ArrayList<Bestellung> bestellungen;

    // Konstruktor fuer Gaeste
    public Profil(String email, String passwort, String vorname, String nachname) {
        this.email = email;
        this.vorname = vorname.substring(0,1).toUpperCase() + vorname.substring(1);;
        this.nachname = nachname.substring(0,1).toUpperCase() + nachname.substring(1);;
        this.passwort = passwort;
        status = "gast";
        id = ++anzahlNutzer;
        warenkorb = new Warenkorb(this);
    }

    // Konstruktor fuer Studenten und Mitarbeiter
    public Profil(String email, String passwort) {
        this.email = email;
        this.passwort = passwort;
        status = ermittleStatus(email);
        id = ++anzahlNutzer;
        extrahiereName(email);
    }

    public static String ermittleStatus(String email) {
        // vorname.nachname000@stud.fh-dortmund.de
        String studentPattern = "^[a-zA-ZäöüÄÖÜß]+\\.[a-zA-ZäöüÄÖÜß]+\\d{3}@stud\\.fh-dortmund\\.de$";
        // vorname.nachname@fh-dortmund.de
        String mitarbeiterPattern = "^[a-zA-ZäöüÄÖÜß]+\\.[a-zA-ZäöüÄÖÜß]+@fh-dortmund\\.de$";

        if (email.matches(studentPattern)) {
            return "student";
        } else if (email.matches(mitarbeiterPattern)) {
            return "mitarbeiter";
        } else if(email.contains("fh-dortmund.de")) {
            return "ungueltig";
        } else {
            return "gast";
        }
    }

    private void extrahiereName(String email) {
        String[] name = email.split("@")[0].split("\\.");     // vorname.nachname000 -> ["vorname","nachname000"]
        String vorname = name[0].substring(0,1).toUpperCase() + name[0].substring(1);
        String nachname = name[1].substring(0,1).toUpperCase() + name[1].substring(1);
        if(status.equals("student") && nachname.length() >= 3) {
            nachname = nachname.substring(0, nachname.length()-3);   // nachname000 -> nachname
        }
        this.vorname = vorname;
        this.nachname = nachname;
    }

    public boolean bestellen(LocalDateTime abholdatum) {
        if(warenkorb.getArtikel().isEmpty()) {
            return false;
        } else {
            bestellungen.add(new Bestellung(warenkorb, abholdatum));
            warenkorb.leeren();
            return true;
        }
    }

    @Override
    public String toString() {
        return("Vorname: " + vorname + " Nachname: " + nachname + " Status: " + status.substring(0,1).toUpperCase() + status.substring(1));
    }

}