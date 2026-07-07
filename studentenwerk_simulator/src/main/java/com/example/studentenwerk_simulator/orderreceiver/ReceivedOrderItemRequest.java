package com.example.studentenwerk_simulator.orderreceiver;

public record ReceivedOrderItemRequest(
        Long gerichtId,
        String name,
        int anzahl,
        double preis
) {}
