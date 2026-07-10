package com.example.mensa_app_backend.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public record StatusUpdate(String status) {}

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(orderService.getOrdersByUser(userId).stream()
                .map(OrderResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication auth, @RequestBody OrderRequest request) {
        Long userId = (Long) auth.getPrincipal();
        // studentName is derived from the user; for now use a placeholder
        Order order = orderService.createOrder(userId, "User " + userId, request);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestBody StatusUpdate update) {
        try {
            return ResponseEntity.ok(OrderResponse.from(orderService.updateStatus(id, update.status())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
