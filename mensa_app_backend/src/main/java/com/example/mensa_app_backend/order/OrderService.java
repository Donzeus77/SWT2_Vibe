package com.example.mensa_app_backend.order;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final MessageChannel ordersOutboundChannel;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository repository,
                        MessageChannel ordersOutboundChannel,
                        ObjectMapper objectMapper) {
        this.repository = repository;
        this.ordersOutboundChannel = ordersOutboundChannel;
        this.objectMapper = objectMapper;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public Order createOrder(Long userId, String studentName, OrderRequest request) {
        Order order = new Order(userId, studentName, 0, request.pickupTime(), generateCode());
        double total = 0;
        for (OrderItemRequest item : request.items()) {
            order.addItem(new OrderItem(item.gerichtId(), item.name(), item.anzahl(), item.preis()));
            total += item.preis() * item.anzahl();
        }
        order.setTotal(total);
        order = repository.save(order);
        publishOrderViaMqtt(order);
        return order;
    }

    public Order updateStatus(Long orderId, String status) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Bestellung nicht gefunden"));
        order.setStatus(status);
        return repository.save(order);
    }

    private void publishOrderViaMqtt(Order order) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("studentName", order.getStudentName());
            payload.put("total", order.getTotal());
            payload.put("pickupTime", order.getPickupTime());
            payload.put("code", order.getCode());
            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("gerichtId", item.getGerichtId());
                itemMap.put("name", item.getName());
                itemMap.put("anzahl", item.getAnzahl());
                itemMap.put("preis", item.getPreis());
                items.add(itemMap);
            }
            payload.put("items", items);
            String json = objectMapper.writeValueAsString(payload);
            ordersOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren der Bestellung via MQTT: " + e.getMessage());
        }
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }
}
