package com.example.mensa_app_backend.bezahlsystem;

import com.example.mensa_app_backend.profil.Profil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Warenkorb {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne private Profil besitzer;
    @OneToMany(mappedBy = "warenkorb", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warenkorb_Item> artikel = new ArrayList<>();
    protected Warenkorb() {}
    public Warenkorb(Profil profil) { this.besitzer = profil; }
    public List<Warenkorb_Item> getArtikel() { return artikel; }
    public void inDenWarenkorb(Long gerichtId, String name, double preis, int anzahl) {
        Warenkorb_Item vorhanden = artikel.stream().filter(i -> gerichtId.equals(i.getGerichtId())).findFirst().orElse(null);
        if (vorhanden == null) { Warenkorb_Item item = new Warenkorb_Item(this, gerichtId, name, preis, anzahl); artikel.add(item); }
        else vorhanden.addAnzahl(anzahl);
    }
    public void leeren() { artikel.clear(); }
    public double getWarenwert() { return artikel.stream().mapToDouble(Warenkorb_Item::berechnePreis).sum(); }
}
