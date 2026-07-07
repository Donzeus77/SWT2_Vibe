package com.example.mensa_app_backend.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gerichtId;
    private String name;
    private int anzahl;
    private double preis;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    protected OrderItem() {}

    public OrderItem(Long gerichtId, String name, int anzahl, double preis) {
        this.gerichtId = gerichtId;
        this.name = name;
        this.anzahl = anzahl;
        this.preis = preis;
    }

    public Long getId() { return id; }
    public Long getGerichtId() { return gerichtId; }
    public String getName() { return name; }
    public int getAnzahl() { return anzahl; }
    public double getPreis() { return preis; }
    public Order getOrder() { return order; }

    public void setOrder(Order order) { this.order = order; }
}
