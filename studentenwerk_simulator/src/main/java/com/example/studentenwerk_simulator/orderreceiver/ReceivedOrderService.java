package com.example.studentenwerk_simulator.orderreceiver;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReceivedOrderService {

    private final List<ReceivedOrder> orders = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<ReceivedOrder> getAllOrders() {
        return List.copyOf(orders);
    }

    public ReceivedOrder receiveOrder(ReceivedOrderRequest request) {
        ReceivedOrder order = new ReceivedOrder(
                nextId.getAndIncrement(),
                request.menuItemId(),
                request.studentName(),
                "EINGEGANGEN"
        );
        orders.add(order);
        return order;
    }
}
