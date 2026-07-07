package com.example.mensa_app_backend.menu;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MenuCache {

    private volatile List<Map<String, Object>> gerichte = new CopyOnWriteArrayList<>();

    public void update(List<Map<String, Object>> gerichte) {
        this.gerichte = new CopyOnWriteArrayList<>(gerichte);
    }

    public List<Map<String, Object>> getAll() {
        return gerichte;
    }
}
