package com.example.mensa_app_backend.menu;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private final List<MenuItem> items = List.of(
            new MenuItem(1L, "Pasta Bolognese", "mit Tomatensoße und Rinderhack", 3.50),
            new MenuItem(2L, "Veganer Salat", "mit Dressing und Croutons", 2.80),
            new MenuItem(3L, "Schnitzel mit Pommes", "paniertes Schweineschnitzel", 4.20),
            new MenuItem(4L, "Suppe des Tages", "wechselt täglich", 1.50)
    );

    public List<MenuItem> getAllItems() {
        return items;
    }

    public Optional<MenuItem> getItemById(Long id) {
        return items.stream().filter(item -> item.id().equals(id)).findFirst();
    }
}
