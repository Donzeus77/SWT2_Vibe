package com.example.mensa_app_backend.preference;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_preferences")
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> dietary = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> allergens = new ArrayList<>();

    protected Preference() {}

    public Preference(Long userId) {
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<String> getDietary() { return dietary; }
    public List<String> getAllergens() { return allergens; }
    public void setDietary(List<String> dietary) { this.dietary = dietary; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
}
