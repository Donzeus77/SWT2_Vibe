package com.example.mensa_app_backend.mensa;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MensaCache {

    private volatile List<Map<String, Object>> mensen = new CopyOnWriteArrayList<>();

    public void update(List<Map<String, Object>> mensen) {
        this.mensen = new CopyOnWriteArrayList<>(mensen);
    }

    public List<Map<String, Object>> getAll() {
        return mensen;
    }
}
