package com.example.studentenwerk_simulator.orderreceiver;

import tools.jackson.databind.ObjectMapper;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class OrderMqttHandler {

    private final ReceivedOrderService receivedOrderService;
    private final ObjectMapper objectMapper;

    public OrderMqttHandler(ReceivedOrderService receivedOrderService, ObjectMapper objectMapper) {
        this.receivedOrderService = receivedOrderService;
        this.objectMapper = objectMapper;
    }

    @ServiceActivator(inputChannel = "ordersInboundChannel")
    public void handleOrder(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            ReceivedOrderRequest request = objectMapper.readValue(payload, ReceivedOrderRequest.class);
            receivedOrderService.receiveOrder(request);
        } catch (Exception e) {
            System.err.println("Fehler beim Verarbeiten der Bestellung via MQTT: " + e.getMessage());
        }
    }
}
