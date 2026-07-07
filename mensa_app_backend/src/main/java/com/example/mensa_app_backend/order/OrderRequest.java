package com.example.mensa_app_backend.order;

import java.util.List;

public record OrderRequest(
        List<OrderItemRequest> items,
        String pickupTime
) {}
