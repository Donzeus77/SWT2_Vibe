package com.example.studentenwerk_simulator.orderreceiver;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/received-orders")
public class ReceivedOrderController {

    private final ReceivedOrderService receivedOrderService;

    public ReceivedOrderController(ReceivedOrderService receivedOrderService) {
        this.receivedOrderService = receivedOrderService;
    }

    @GetMapping
    public List<ReceivedOrder> getAllOrders() {
        return receivedOrderService.getAllOrders();
    }

    @PostMapping
    public ReceivedOrder receiveOrder(@RequestBody ReceivedOrderRequest request) {
        return receivedOrderService.receiveOrder(request);
    }
}
