package com.example.mensa_app_backend.order;

public record OrderItemRequest(
        Long gerichtId,
        String name,
        int anzahl,
        double preis
) {}
