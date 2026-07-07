package com.example.mensa_app_backend.menu;

import com.example.mensa_app_backend.mensa.MensaCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

@Configuration
public class MenuMqttHandler {

    @Bean
    public MenuCache menuCache() {
        return new MenuCache();
    }

    @Bean
    public MensaCache mensaCache() {
        return new MensaCache();
    }

    @Component
    public static class SpeiseplanHandler {
        private final MenuCache cache;
        private final ObjectMapper objectMapper;

        public SpeiseplanHandler(MenuCache cache, ObjectMapper objectMapper) {
            this.cache = cache;
            this.objectMapper = objectMapper;
        }

        @ServiceActivator(inputChannel = "speiseplanInboundChannel")
        public void handleSpeiseplan(Message<?> message) {
            try {
                String payload = message.getPayload().toString();
                List<Map<String, Object>> gerichte = objectMapper.readValue(
                        payload, new TypeReference<List<Map<String, Object>>>() {});
                cache.update(gerichte);
            } catch (Exception e) {
                System.err.println("Fehler beim Empfangen des Speiseplans via MQTT: " + e.getMessage());
            }
        }
    }

    @Component
    public static class MensaHandler {
        private final MensaCache cache;
        private final ObjectMapper objectMapper;

        public MensaHandler(MensaCache cache, ObjectMapper objectMapper) {
            this.cache = cache;
            this.objectMapper = objectMapper;
        }

        @ServiceActivator(inputChannel = "mensenInboundChannel")
        public void handleMensen(Message<?> message) {
            try {
                String payload = message.getPayload().toString();
                List<Map<String, Object>> mensen = objectMapper.readValue(
                        payload, new TypeReference<List<Map<String, Object>>>() {});
                cache.update(mensen);
            } catch (Exception e) {
                System.err.println("Fehler beim Empfangen der Mensen via MQTT: " + e.getMessage());
            }
        }
    }
}
