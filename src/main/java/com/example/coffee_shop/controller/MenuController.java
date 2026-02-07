package com.example.coffee_shop.controller;

import com.example.coffee_shop.model.Drink;
import com.example.coffee_shop.repository.DrinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final DrinkRepository drinkRepository;

    @GetMapping
    public ResponseEntity<List<Drink>> getMenu() {
        return ResponseEntity.ok(drinkRepository.findAll());
    }
}
