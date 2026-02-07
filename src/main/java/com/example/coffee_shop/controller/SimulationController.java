package com.example.coffee_shop.controller;

import com.example.coffee_shop.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/run")
    public ResponseEntity<String> runSimulation() {
        String result = simulationService.runSimulation();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/process")
    public ResponseEntity<String> processOrders() {
        String result = simulationService.processSimulationOrders();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(simulationService.getSimulationStats());
    }
}
