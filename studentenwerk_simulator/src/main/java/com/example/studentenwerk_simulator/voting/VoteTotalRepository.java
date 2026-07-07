package com.example.studentenwerk_simulator.voting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteTotalRepository extends JpaRepository<VoteTotal, Long> {
    Optional<VoteTotal> findByGerichtId(Long gerichtId);
}
