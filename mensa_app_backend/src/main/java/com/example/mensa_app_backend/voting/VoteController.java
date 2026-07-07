package com.example.mensa_app_backend.voting;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping
    public Map<Long, Integer> getVoteCounts() {
        return voteService.getVoteCounts();
    }

    @GetMapping("/my")
    public ResponseEntity<List<Long>> getMyVotes(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(voteService.getMyVotes(userId));
    }

    @PostMapping("/{gerichtId}")
    public ResponseEntity<Void> castVote(Authentication auth, @PathVariable Long gerichtId) {
        Long userId = (Long) auth.getPrincipal();
        try {
            voteService.castVote(userId, gerichtId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }
}
