package com.example.studentenwerk_simulator.orderreceiver;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceivedOrderService {

    private final ReceivedOrderRepository repository;

    public ReceivedOrderService(ReceivedOrderRepository repository) {
        this.repository = repository;
    }

    public List<ReceivedOrder> getAllOrders() {
        return repository.findAll();
    }

    public ReceivedOrder receiveOrder(ReceivedOrderRequest request) {
        ReceivedOrder order = new ReceivedOrder(
                request.studentName(),
                request.total(),
                request.pickupTime(),
                request.code()
        );
        if (request.items() != null) {
            for (ReceivedOrderItemRequest item : request.items()) {
                order.addItem(new ReceivedOrderItem(
                        item.gerichtId(), item.name(), item.anzahl(), item.preis()
                ));
            }
        }
        return repository.save(order);
    }
}
