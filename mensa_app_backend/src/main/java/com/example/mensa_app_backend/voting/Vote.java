package com.example.mensa_app_backend.voting;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "gerichtId"}))
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long gerichtId;

    protected Vote() {}

    public Vote(Long userId, Long gerichtId) {
        this.userId = userId;
        this.gerichtId = gerichtId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getGerichtId() { return gerichtId; }
}
