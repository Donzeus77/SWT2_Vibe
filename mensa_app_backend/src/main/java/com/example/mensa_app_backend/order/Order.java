package com.example.mensa_app_backend.order;

public record Order(Long id, Long menuItemId, String studentName, String status) {}
