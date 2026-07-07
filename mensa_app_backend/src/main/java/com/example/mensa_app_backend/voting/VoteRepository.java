package com.example.mensa_app_backend.voting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByUserId(Long userId);
    Optional<Vote> findByUserIdAndGerichtId(Long userId, Long gerichtId);
    List<Vote> findByGerichtId(Long gerichtId);
}
