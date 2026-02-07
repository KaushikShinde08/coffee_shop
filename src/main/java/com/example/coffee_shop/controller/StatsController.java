package com.example.coffee_shop.controller;

import com.example.coffee_shop.dto.StatsDTO;
import com.example.coffee_shop.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsDTO> getStatistics() {
        return ResponseEntity.ok(statsService.calculateStatistics());
    }
}
