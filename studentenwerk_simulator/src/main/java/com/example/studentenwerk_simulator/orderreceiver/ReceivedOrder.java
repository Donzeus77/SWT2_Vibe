package com.example.studentenwerk_simulator.orderreceiver;

public record ReceivedOrder(Long id, Long menuItemId, String studentName, String status) {}
