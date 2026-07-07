package com.example.mensa_app_backend.menu;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MenuService {

    private final MenuCache menuCache;

    public MenuService(MenuCache menuCache) {
        this.menuCache = menuCache;
    }

    public List<Map<String, Object>> getAllItems() {
        return menuCache.getAll();
    }

    public Optional<Map<String, Object>> getItemById(Long id) {
        return menuCache.getAll().stream()
                .filter(item -> id.equals(item.get("id")))
                .findFirst();
    }
}
