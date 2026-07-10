package com.example.mensa_app_backend.bezahlsystem;

import com.example.mensa_app_backend.profil.Profil;
import com.example.mensa_app_backend.profil.ProfilRepository;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/orders")
public class BestellungController {
    private final ProfilRepository profileRepository; private final BestellungRepository bestellungRepository; private final MessageChannel ordersOutboundChannel; private final ObjectMapper objectMapper;
    public BestellungController(ProfilRepository profileRepository, BestellungRepository bestellungRepository, MessageChannel ordersOutboundChannel, ObjectMapper objectMapper) { this.profileRepository = profileRepository; this.bestellungRepository = bestellungRepository; this.ordersOutboundChannel = ordersOutboundChannel; this.objectMapper = objectMapper; }
    public record ItemRequest(Long gerichtId, String name, int anzahl, double preis) {}
    public record BestellungRequest(String email, List<ItemRequest> items, String pickupTime) {}
    public record ItemResponse(Long gerichtId, String name, int anzahl, double preis) {}
    public record BestellungResponse(Long id, List<ItemResponse> items, double total, String status, String pickupTime, String code) { static BestellungResponse from(Bestellung b) { return new BestellungResponse(b.getBestellnr(), b.getArtikel().stream().map(i -> new ItemResponse(i.getGerichtId(), i.getName(), i.getAnzahl(), i.getPreis())).toList(), b.getGesamtpreis(), b.getStatus(), b.getAbholdatum(), b.getCode()); } }
    @GetMapping public ResponseEntity<List<BestellungResponse>> getOrders(@RequestParam String email) { return ResponseEntity.ok(bestellungRepository.findByProfilEmailOrderByBestellnrDesc(email).stream().map(BestellungResponse::from).toList()); }
    @PostMapping public ResponseEntity<BestellungResponse> create(@RequestBody BestellungRequest request) {
        Profil profil = profileRepository.findByEmail(request.email()).orElse(null); if (profil == null || request.items() == null || request.items().isEmpty()) return ResponseEntity.badRequest().build();
        Warenkorb cart = profil.getWarenkorb(); cart.leeren(); for (ItemRequest item : request.items()) cart.inDenWarenkorb(item.gerichtId(), item.name(), item.preis(), item.anzahl());
        Bestellung bestellung = new Bestellung(profil, cart, request.pickupTime(), code()); profil.addBestellung(bestellung); cart.leeren();
        bestellung = bestellungRepository.save(bestellung); publish(bestellung, profil); return ResponseEntity.ok(BestellungResponse.from(bestellung));
    }
    @PatchMapping("/{id}") public ResponseEntity<BestellungResponse> update(@PathVariable Long id, @RequestBody String status) { return bestellungRepository.findById(id).map(b -> { b.setStatus(status.replaceAll("[\"{}]", "").replace("status:", "")); return ResponseEntity.ok(BestellungResponse.from(bestellungRepository.save(b))); }).orElseGet(() -> ResponseEntity.notFound().build()); }
    private String code() { return Long.toString(System.nanoTime(), 36).toUpperCase().substring(0, 5); }
    private void publish(Bestellung bestellung, Profil profil) { try { ordersOutboundChannel.send(MessageBuilder.withPayload(objectMapper.writeValueAsString(Map.of("studentName", profil.getVorname() + " " + profil.getNachname(), "total", bestellung.getGesamtpreis(), "pickupTime", bestellung.getAbholdatum(), "code", bestellung.getCode(), "items", bestellung.getArtikel().stream().map(i -> Map.of("gerichtId", i.getGerichtId(), "name", i.getName(), "anzahl", i.getAnzahl(), "preis", i.getPreis())).toList()))).build()); } catch (Exception ignored) {} }
}
