package com.example.studentenwerk_simulator.orderreceiver;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "received_order")
public class ReceivedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName;
    private String status;
    private double total;
    private String pickupTime;
    private String code;
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivedOrderItem> items = new ArrayList<>();

    protected ReceivedOrder() {}

    public ReceivedOrder(String studentName, double total, String pickupTime, String code) {
        this.studentName = studentName;
        this.status = "EINGEGANGEN";
        this.total = total;
        this.pickupTime = pickupTime;
        this.code = code;
        this.receivedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getStudentName() { return studentName; }
    public String getStatus() { return status; }
    public double getTotal() { return total; }
    public String getPickupTime() { return pickupTime; }
    public String getCode() { return code; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public List<ReceivedOrderItem> getItems() { return items; }

    public void addItem(ReceivedOrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
