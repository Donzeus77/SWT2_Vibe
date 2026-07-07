package com.example.mensa_app_backend.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String studentName;
    private String status;
    private double total;
    private String pickupTime;
    private String code;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    public Order(Long userId, String studentName, double total, String pickupTime, String code) {
        this.userId = userId;
        this.studentName = studentName;
        this.status = "OFFEN";
        this.total = total;
        this.pickupTime = pickupTime;
        this.code = code;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getStudentName() { return studentName; }
    public String getStatus() { return status; }
    public double getTotal() { return total; }
    public String getPickupTime() { return pickupTime; }
    public String getCode() { return code; }
    public List<OrderItem> getItems() { return items; }

    public void setStatus(String status) { this.status = status; }
    public void setTotal(double total) { this.total = total; }
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
