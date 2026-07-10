package com.example.mensa_app_backend.order;

import java.util.List;

public record OrderResponse(
        Long id,
        List<OrderItemResponse> items,
        double total,
        String status,
        String pickupTime,
        String code
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotal(),
                order.getStatus(),
                order.getPickupTime(),
                order.getCode()
        );
    }

    public record OrderItemResponse(Long gerichtId, String name, int anzahl, double preis) {
        static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getGerichtId(), item.getName(), item.getAnzahl(), item.getPreis());
        }
    }
}
