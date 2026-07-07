package com.example.studentenwerk_simulator.config;

import com.example.studentenwerk_simulator.gericht.BeilageRepository;
import com.example.studentenwerk_simulator.gericht.Gericht;
import com.example.studentenwerk_simulator.gericht.HauptspeiseRepository;
import com.example.studentenwerk_simulator.mensa.Mensa;
import com.example.studentenwerk_simulator.mensa.MensaRepository;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MqttPublisher {

    private final MessageChannel speiseplanOutboundChannel;
    private final MessageChannel mensenOutboundChannel;
    private final HauptspeiseRepository hauptspeiseRepository;
    private final BeilageRepository beilageRepository;
    private final MensaRepository mensaRepository;
    private final ObjectMapper objectMapper;

    public MqttPublisher(
            MessageChannel speiseplanOutboundChannel,
            MessageChannel mensenOutboundChannel,
            HauptspeiseRepository hauptspeiseRepository,
            BeilageRepository beilageRepository,
            MensaRepository mensaRepository,
            ObjectMapper objectMapper) {
        this.speiseplanOutboundChannel = speiseplanOutboundChannel;
        this.mensenOutboundChannel = mensenOutboundChannel;
        this.hauptspeiseRepository = hauptspeiseRepository;
        this.beilageRepository = beilageRepository;
        this.mensaRepository = mensaRepository;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishOnStartup() {
        publishSpeiseplan();
        publishMensen();
    }

    public void publishSpeiseplan() {
        try {
            List<Map<String, Object>> gerichte = new ArrayList<>();
            for (Gericht g : hauptspeiseRepository.findAll()) {
                gerichte.add(gerichtToMap(g));
            }
            for (Gericht g : beilageRepository.findAll()) {
                gerichte.add(gerichtToMap(g));
            }
            String json = objectMapper.writeValueAsString(gerichte);
            speiseplanOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren des Speiseplans: " + e.getMessage());
        }
    }

    public void publishMensen() {
        try {
            List<Map<String, Object>> mensen = new ArrayList<>();
            for (Mensa m : mensaRepository.findAll()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.getId());
                map.put("name", m.getName());
                map.put("campus", m.getCampus());
                map.put("adresse", m.getAdresse());
                map.put("oeffnungszeiten", m.getOeffnungszeiten());
                map.put("auslastung", m.getAuslastung());
                mensen.add(map);
            }
            String json = objectMapper.writeValueAsString(mensen);
            mensenOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren der Mensen: " + e.getMessage());
        }
    }

    private Map<String, Object> gerichtToMap(Gericht g) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", g.getId());
        map.put("name", g.getName());
        map.put("beschreibung", g.getBeschreibung());
        map.put("preisStudent", g.getPreisStudent());
        map.put("preisGast", g.getPreisGast());
        map.put("allergene", g.getAllergene());
        map.put("tags", g.getTags());
        map.put("typ", g.getTyp());
        return map;
    }
}
