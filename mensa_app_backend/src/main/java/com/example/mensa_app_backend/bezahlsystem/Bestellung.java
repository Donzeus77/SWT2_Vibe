package com.example.mensa_app_backend.bezahlsystem;

import com.example.mensa_app_backend.profil.Profil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Bestellung {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long bestellnr;
    private LocalDateTime bestelldatum = LocalDateTime.now();
    private String abholdatum;
    private String status = "OFFEN";
    private String code;
    @ManyToOne private Profil profil;
    @OneToMany(mappedBy = "bestellung", cascade = CascadeType.ALL, orphanRemoval = true) private List<Warenkorb_Item> artikel = new ArrayList<>();
    protected Bestellung() {}
    public Bestellung(Profil profil, Warenkorb warenkorb, String abholdatum, String code) { this.profil = profil; this.abholdatum = abholdatum; this.code = code; for (Warenkorb_Item item : warenkorb.getArtikel()) artikel.add(new Warenkorb_Item(this, item)); }
    public Long getBestellnr() { return bestellnr; } public String getAbholdatum() { return abholdatum; } public String getStatus() { return status; } public String getCode() { return code; } public List<Warenkorb_Item> getArtikel() { return artikel; }
    public double getGesamtpreis() { return artikel.stream().mapToDouble(Warenkorb_Item::berechnePreis).sum(); }
    public void setStatus(String status) { this.status = status; } public void setProfil(Profil profil) { this.profil = profil; }
}
