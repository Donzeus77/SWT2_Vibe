package com.example.mensa_app_backend.profil;

import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/votes")
public class AbstimmungController {
    private final AbstimmungRepository repository; private final MessageChannel votesOutboundChannel; private final ObjectMapper objectMapper;
    public AbstimmungController(AbstimmungRepository repository, MessageChannel votesOutboundChannel, ObjectMapper objectMapper) { this.repository = repository; this.votesOutboundChannel = votesOutboundChannel; this.objectMapper = objectMapper; }
    private Abstimmung aktuelle() { return repository.findAll().stream().findFirst().orElseGet(() -> repository.save(new Abstimmung())); }
    @GetMapping public Map<Long, Integer> counts() { return aktuelle().getStimmen(); }
    @GetMapping("/my") public List<Long> mine(@RequestParam String email) { return aktuelle().getVotesVon(email).stream().toList(); }
    @PostMapping("/{gerichtId}") public ResponseEntity<Void> vote(@PathVariable Long gerichtId, @RequestParam String email) { Abstimmung a = aktuelle(); if (!a.abstimmen(email, gerichtId)) return ResponseEntity.status(409).build(); repository.save(a); try { votesOutboundChannel.send(MessageBuilder.withPayload(objectMapper.writeValueAsString(a.getStimmen())).build()); } catch (Exception ignored) {} return ResponseEntity.ok().build(); }
}
