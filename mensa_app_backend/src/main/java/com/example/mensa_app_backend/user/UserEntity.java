package com.example.mensa_app_backend.user;

import com.example.mensa_app_backend.profil.Profil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String passwordHash;
    private String vorname;
    private String nachname;
    private String type;

    protected UserEntity() {}

    public UserEntity(String email, String passwordHash, String vorname, String nachname, String type) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.vorname = vorname;
        this.nachname = nachname;
        this.type = type;
    }

    public static UserEntity fromRegistration(String email, String passwordHash, String vorname, String nachname) {
        String type = Profil.ermittleStatus(email);
        return new UserEntity(email, passwordHash, vorname, nachname, type);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getVorname() { return vorname; }
    public String getNachname() { return nachname; }
    public String getType() { return type; }
}
