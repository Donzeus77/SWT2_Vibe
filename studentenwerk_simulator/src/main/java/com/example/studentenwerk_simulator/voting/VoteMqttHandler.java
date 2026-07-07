package com.example.studentenwerk_simulator.voting;

import tools.jackson.databind.ObjectMapper;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VoteMqttHandler {

    private final VoteTotalRepository repository;
    private final ObjectMapper objectMapper;

    public VoteMqttHandler(VoteTotalRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @ServiceActivator(inputChannel = "votesInboundChannel")
    public void handleVotes(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            @SuppressWarnings("unchecked")
            Map<String, Integer> voteMap = objectMapper.readValue(payload, Map.class);
            for (Map.Entry<String, Integer> entry : voteMap.entrySet()) {
                Long gerichtId = Long.parseLong(entry.getKey());
                int count = entry.getValue();
                VoteTotal voteTotal = repository.findByGerichtId(gerichtId)
                        .orElseGet(() -> new VoteTotal(gerichtId, 0));
                voteTotal.setCount(count);
                repository.save(voteTotal);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Verarbeiten der Votes via MQTT: " + e.getMessage());
        }
    }
}
