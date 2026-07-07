package com.example.mensa_app_backend.order;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<Order> getAllOrders() {
        return List.copyOf(orders);
    }

    public Optional<Order> getOrderById(Long id) {
        return orders.stream().filter(o -> o.id().equals(id)).findFirst();
    }

    public Order createOrder(OrderRequest request) {
        Order order = new Order(nextId.getAndIncrement(), request.menuItemId(), request.studentName(), "OFFEN");
        orders.add(order);
        return order;
    }
}
