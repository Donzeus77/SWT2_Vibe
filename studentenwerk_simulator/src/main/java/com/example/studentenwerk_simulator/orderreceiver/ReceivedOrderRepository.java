package com.example.studentenwerk_simulator.orderreceiver;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivedOrderRepository extends JpaRepository<ReceivedOrder, Long> {}
