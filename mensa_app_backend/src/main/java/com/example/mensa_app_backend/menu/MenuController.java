package com.example.mensa_app_backend.menu;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllMenuItems() {
        return menuService.getAllItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMenuItemById(@PathVariable Long id) {
        return menuService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
