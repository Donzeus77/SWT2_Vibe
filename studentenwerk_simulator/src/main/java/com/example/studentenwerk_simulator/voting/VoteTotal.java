package com.example.studentenwerk_simulator.voting;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vote_total")
public class VoteTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gerichtId;
    private int count;

    protected VoteTotal() {}

    public VoteTotal(Long gerichtId, int count) {
        this.gerichtId = gerichtId;
        this.count = count;
    }

    public Long getId() { return id; }
    public Long getGerichtId() { return gerichtId; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
