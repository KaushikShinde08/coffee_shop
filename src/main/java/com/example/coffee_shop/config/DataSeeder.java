package com.example.coffee_shop.config;

import com.example.coffee_shop.model.Barista;
import com.example.coffee_shop.model.Drink;
import com.example.coffee_shop.repository.BaristaRepository;
import com.example.coffee_shop.repository.DrinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class DataSeeder {

    private final DrinkRepository drinkRepository;
    private final BaristaRepository baristaRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Seed Drinks
            if (drinkRepository.count() == 0) {
                drinkRepository.saveAll(Arrays.asList(
                        new Drink(null, "Cold Brew", 1, 4.50, 0.25),
                        new Drink(null, "Espresso", 2, 3.00, 0.20),
                        new Drink(null, "Americano", 2, 3.50, 0.15),
                        new Drink(null, "Cappuccino", 4, 4.50, 0.20),
                        new Drink(null, "Latte", 4, 4.50, 0.12),
                        new Drink(null, "Specialty (Mocha)", 6, 5.50, 0.08)));
            }

            // Seed Baristas
            // Seed Baristas
            if (baristaRepository.findByStatus(com.example.coffee_shop.model.BaristaStatus.ACTIVE).isEmpty()) {
                log.info("No active baristas found. Seeding baristas...");
                baristaRepository.saveAll(Arrays.asList(
                        new Barista(null, "Alice", com.example.coffee_shop.model.BaristaStatus.ACTIVE, 0),
                        new Barista(null, "Bob", com.example.coffee_shop.model.BaristaStatus.ACTIVE, 0),
                        new Barista(null, "Charlie", com.example.coffee_shop.model.BaristaStatus.ACTIVE, 0)));
            } else {
                log.info("Active baristas already exist. Skipping seeding.");
            }
        };
    }
}
