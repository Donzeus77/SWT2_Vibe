package com.example.studentenwerk_simulator.orderreceiver;

import java.util.List;

public record ReceivedOrderRequest(
        String studentName,
        double total,
        String pickupTime,
        String code,
        List<ReceivedOrderItemRequest> items
) {}
