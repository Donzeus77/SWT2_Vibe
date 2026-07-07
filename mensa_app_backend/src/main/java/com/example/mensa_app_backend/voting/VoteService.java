package com.example.mensa_app_backend.voting;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VoteService {

    private final VoteRepository repository;
    private final MessageChannel votesOutboundChannel;
    private final ObjectMapper objectMapper;

    public VoteService(VoteRepository repository,
                       MessageChannel votesOutboundChannel,
                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.votesOutboundChannel = votesOutboundChannel;
        this.objectMapper = objectMapper;
    }

    public Map<Long, Integer> getVoteCounts() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(Vote::getGerichtId, Collectors.summingInt(v -> 1)))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().longValue(), Map.Entry::getValue));
    }

    public List<Long> getMyVotes(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(Vote::getGerichtId)
                .toList();
    }

    public void castVote(Long userId, Long gerichtId) {
        if (repository.findByUserIdAndGerichtId(userId, gerichtId).isPresent()) {
            throw new IllegalStateException("Bereits abgestimmt");
        }
        repository.save(new Vote(userId, gerichtId));
        publishVotesViaMqtt();
    }

    private void publishVotesViaMqtt() {
        try {
            Map<Long, Integer> counts = getVoteCounts();
            Map<String, Integer> payload = new HashMap<>();
            for (var entry : counts.entrySet()) {
                payload.put(entry.getKey().toString(), entry.getValue());
            }
            String json = objectMapper.writeValueAsString(payload);
            votesOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren der Votes via MQTT: " + e.getMessage());
        }
    }
}
