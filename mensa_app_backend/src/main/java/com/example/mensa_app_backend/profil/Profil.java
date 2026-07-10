package com.example.mensa_app_backend.profil;

import com.example.mensa_app_backend.bezahlsystem.Bestellung;
import com.example.mensa_app_backend.bezahlsystem.Warenkorb;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class Profil {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    private String passwort;
    private String vorname;
    private String nachname;
    private String status;

    @OneToOne(mappedBy = "besitzer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Warenkorb warenkorb;
    @OneToMany(mappedBy = "profil", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bestellung> bestellungen = new ArrayList<>();

    protected Profil() {}

    public Profil(String email, String passwort, String vorname, String nachname) {
        this.email = email;
        this.passwort = passwort;
        this.vorname = grossschreiben(vorname);
        this.nachname = grossschreiben(nachname);
        this.status = "gast";
        this.warenkorb = new Warenkorb(this);
    }

    public Profil(String email, String passwort) {
        this.email = email;
        this.passwort = passwort;
        this.status = ermittleStatus(email);
        extrahiereName(email);
        this.warenkorb = new Warenkorb(this);
    }

    public static String ermittleStatus(String email) {
        String studentPattern = "^[a-zA-ZäöüÄÖÜß]+\\.[a-zA-ZäöüÄÖÜß]+\\d{3}@stud\\.fh-dortmund\\.de$";
        String mitarbeiterPattern = "^[a-zA-ZäöüÄÖÜß]+\\.[a-zA-ZäöüÄÖÜß]+@fh-dortmund\\.de$";
        if (email.matches(studentPattern)) return "student";
        if (email.matches(mitarbeiterPattern)) return "mitarbeiter";
        if (email.contains("fh-dortmund.de")) return "ungueltig";
        return "gast";
    }

    private void extrahiereName(String email) {
        String[] name = email.split("@")[0].split("\\.");
        if (name.length < 2) { this.vorname = grossschreiben(name[0]); this.nachname = ""; return; }
        this.vorname = grossschreiben(name[0]);
        String nachnameAusMail = name[1];
        if ("student".equals(status) && nachnameAusMail.length() >= 3) nachnameAusMail = nachnameAusMail.substring(0, nachnameAusMail.length() - 3);
        this.nachname = grossschreiben(nachnameAusMail);
    }

    private static String grossschreiben(String text) {
        return text == null || text.isBlank() ? "" : text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public void addBestellung(Bestellung bestellung) { bestellungen.add(bestellung); bestellung.setProfil(this); }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswort() { return passwort; }
    public String getVorname() { return vorname; }
    public String getNachname() { return nachname; }
    public String getStatus() { return status; }
    public Warenkorb getWarenkorb() { return warenkorb; }
    public List<Bestellung> getBestellungen() { return bestellungen; }
}
