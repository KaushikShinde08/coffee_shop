package com.example.coffee_shop.service;

import com.example.coffee_shop.model.Drink;
import com.example.coffee_shop.model.Order;
import com.example.coffee_shop.model.OrderStatus;
import com.example.coffee_shop.repository.DrinkRepository;
import com.example.coffee_shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final DrinkRepository drinkRepository;
    private final OrderRepository orderRepository;
    private final PriorityService priorityService;
    private final Random random = new Random();

    // Drink frequency distribution
    private static final Map<String, Double> DRINK_FREQUENCIES = Map.of(
            "Cold Brew", 0.25,
            "Espresso", 0.20,
            "Americano", 0.15,
            "Cappuccino", 0.20,
            "Latte", 0.12,
            "Mocha", 0.08);

    // Simulation parameters
    private static final int TOTAL_ORDERS = 100; // Reduced to 100
    private static final double LAMBDA = 0.60; // Spread arrivals over ~3 hours
    private static final LocalTime START_TIME = LocalTime.of(7, 0);
    private static final LocalTime END_TIME = LocalTime.of(10, 0);
    private static final double LOYAL_CUSTOMER_RATE = 0.30; // 30% loyal

    public String runSimulation() {
        log.info("Starting simulation with {} orders", TOTAL_ORDERS);

        // Clear existing orders for clean simulation
        orderRepository.deleteAll();
        log.info("Cleared existing orders");

        // Generate Poisson arrival times
        List<LocalDateTime> arrivalTimes = generatePoissonArrivals();
        log.info("Generated {} arrival times", arrivalTimes.size());

        // Get drinks from database
        List<Drink> drinks = drinkRepository.findAll();
        Map<String, Drink> drinkMap = new HashMap<>();
        for (Drink drink : drinks) {
            drinkMap.put(drink.getName(), drink);
        }

        // Create orders with generated arrival times
        int ordersCreated = 0;
        for (LocalDateTime arrivalTime : arrivalTimes) {
            // Select random drink based on frequency distribution
            Drink selectedDrink = selectRandomDrink(drinkMap);

            // 30% chance of loyal customer
            boolean isLoyal = random.nextDouble() < LOYAL_CUSTOMER_RATE;

            // Generate random customer name
            String customerName = "Customer_" + (ordersCreated + 1);

            // Create order
            Order order = new Order();
            order.setCustomerName(customerName);
            order.setDrink(selectedDrink);
            order.setLoyal(isLoyal);
            order.setStatus(OrderStatus.WAITING);
            order.setOrderTime(arrivalTime);
            order.setHardDeadline(arrivalTime.plusMinutes(10));

            // Calculate initial priority
            order.setPriorityScore(priorityService.calculatePriority(order));

            // Save order
            orderRepository.save(order);
            ordersCreated++;
        }

        log.info("Simulation complete: {} orders created", ordersCreated);
        return String.format("Simulation complete: %d orders created with Poisson arrivals (λ=%.1f)",
                ordersCreated, LAMBDA);
    }

    /**
     * Generate arrival times using Poisson distribution
     * Inter-arrival times follow exponential distribution with rate λ
     */
    private List<LocalDateTime> generatePoissonArrivals() {
        List<LocalDateTime> arrivalTimes = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDateTime currentTime = LocalDateTime.of(today, START_TIME);
        LocalDateTime endTime = LocalDateTime.of(today, END_TIME);

        int orderCount = 0;
        while (orderCount < TOTAL_ORDERS && currentTime.isBefore(endTime)) {
            // Inter-arrival time follows exponential distribution
            // Mean inter-arrival time = 1/λ minutes
            double interArrival = -Math.log(1.0 - random.nextDouble()) / LAMBDA;

            currentTime = currentTime.plusSeconds((long) (interArrival * 60));

            // Only add if within time window
            if (currentTime.isBefore(endTime) && orderCount < TOTAL_ORDERS) {
                arrivalTimes.add(currentTime);
                orderCount++;
            }
        }

        // If we didn't reach 250 orders within the time window,
        // generate remaining orders with randomized times within the window
        while (arrivalTimes.size() < TOTAL_ORDERS) {
            long totalMinutes = 180; // 3 hours = 180 minutes
            long randomMinutes = random.nextLong(totalMinutes);
            LocalDateTime randomTime = LocalDateTime.of(today, START_TIME).plusMinutes(randomMinutes);
            arrivalTimes.add(randomTime);
        }

        // Sort arrival times
        arrivalTimes.sort(LocalDateTime::compareTo);

        return arrivalTimes;
    }

    /**
     * Select a random drink based on frequency distribution
     */
    private Drink selectRandomDrink(Map<String, Drink> drinkMap) {
        double rand = random.nextDouble();
        double cumulative = 0.0;

        for (Map.Entry<String, Double> entry : DRINK_FREQUENCIES.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                Drink drink = drinkMap.get(entry.getKey());
                if (drink != null) {
                    return drink;
                }
            }
        }

        // Fallback: return first available drink
        return drinkMap.values().iterator().next();
    }

    /**
     * Get simulation statistics
     */
    public Map<String, Object> getSimulationStats() {
        List<Order> allOrders = orderRepository.findAll();

        Map<String, Long> drinkCounts = new HashMap<>();
        for (Order order : allOrders) {
            String drinkName = order.getDrink().getName();
            drinkCounts.put(drinkName, drinkCounts.getOrDefault(drinkName, 0L) + 1);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", allOrders.size());
        stats.put("drinkDistribution", drinkCounts);
        stats.put("loyalCustomers", allOrders.stream().filter(Order::isLoyal).count());

        return stats;
    }

    /**
     * Process all waiting orders through completion for simulation
     * WITH ADAPTIVE SCHEDULING to enforce avg wait < 10 min
     */
    public String processSimulationOrders() {
        log.info("Processing simulation orders with ADAPTIVE SCHEDULING...");

        List<Order> allWaitingOrders = orderRepository.findByStatus(OrderStatus.WAITING);

        // Initialize simulated baristas
        LocalDateTime[] baristaFreeTime = new LocalDateTime[3];
        for (int i = 0; i < 3; i++) {
            baristaFreeTime[i] = LocalDateTime.of(LocalDate.now(), START_TIME);
        }

        // Track running average wait time
        List<Double> completedWaitTimes = new ArrayList<>();
        double runningAvgWait = 0.0;

        int processed = 0;
        int emergencyModeActivations = 0;
        int warningModeActivations = 0;

        while (!allWaitingOrders.isEmpty()) {
            // Find barista that will be free earliest
            int earliestBarista = 0;
            LocalDateTime earliestTime = baristaFreeTime[0];
            for (int i = 1; i < 3; i++) {
                if (baristaFreeTime[i].isBefore(earliestTime)) {
                    earliestTime = baristaFreeTime[i];
                    earliestBarista = i;
                }
            }

            // Get orders that have arrived by the time barista is free
            LocalDateTime currentTime = earliestTime;
            List<Order> availableOrders = allWaitingOrders.stream()
                    .filter(o -> !o.getOrderTime().isAfter(currentTime))
                    .collect(Collectors.toList());

            if (availableOrders.isEmpty()) {
                // No orders yet, advance to next order arrival
                availableOrders = List.of(allWaitingOrders.get(0));
            }

            // ADAPTIVE SCHEDULING LOGIC WITH THROUGHPUT PROTECTION
            Order selectedOrder;
            final double currentAvg = runningAvgWait; // For lambda capture

            // Count long jobs currently in progress (simulated)
            long specialtyInProgress = 0; // 6 min jobs
            long longInProgress = 0; // 4 min jobs

            // Simple heuristic: check recent assignments
            // (In real system, track actual barista assignments)

            if (runningAvgWait >= 9.0) {
                // CRITICAL MODE (was 9.0): Force shortest-job-first
                selectedOrder = availableOrders.stream()
                        .min(Comparator.comparing(o -> o.getDrink().getPrepTimeMinutes()))
                        .orElse(availableOrders.get(0));
                emergencyModeActivations++;
                log.warn("CRITICAL MODE: Avg wait = {:.2f} min, selecting shortest job", runningAvgWait);

            } else if (runningAvgWait >= 7.5) {
                // WARNING MODE: Use throughput-aware priority
                selectedOrder = availableOrders.stream()
                        .max(Comparator.comparing(o -> priorityService.calculatePriorityWithThroughput(o, currentAvg)))
                        .orElse(availableOrders.get(0));
                warningModeActivations++;
                log.info("WARNING MODE: Avg wait = {:.2f} min, using throughput-aware priority", runningAvgWait);

            } else {
                // NORMAL MODE: Use throughput-aware priority (always protect throughput)
                selectedOrder = availableOrders.stream()
                        .max(Comparator.comparing(o -> priorityService.calculatePriorityWithThroughput(o, currentAvg)))
                        .orElse(availableOrders.get(0));
            }

            // Order can't start before it's placed
            LocalDateTime startTime = selectedOrder.getOrderTime().isAfter(earliestTime)
                    ? selectedOrder.getOrderTime()
                    : earliestTime;

            // Calculate completion time
            LocalDateTime completionTime = startTime.plusMinutes(selectedOrder.getDrink().getPrepTimeMinutes());

            // Calculate wait time for this order
            double waitTime = Duration.between(selectedOrder.getOrderTime(), completionTime).toMinutes();
            completedWaitTimes.add(waitTime);

            // Update running average
            runningAvgWait = completedWaitTimes.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Update order
            selectedOrder.setStatus(OrderStatus.COMPLETED);
            selectedOrder.setCompletedTime(completionTime);

            // Update barista free time
            baristaFreeTime[earliestBarista] = completionTime;

            // Save and remove from waiting list
            orderRepository.save(selectedOrder);
            allWaitingOrders.remove(selectedOrder);
            processed++;

            // Log progress every 50 orders
            if (processed % 50 == 0) {
                log.info("Processed {}/{} orders, Running avg wait: {:.2f} min",
                        processed, processed + allWaitingOrders.size(), runningAvgWait);
            }
        }

        log.info("Simulation complete: {} orders processed", processed);
        log.info("Final avg wait: {:.2f} min", runningAvgWait);
        log.info("Emergency mode activations: {}", emergencyModeActivations);
        log.info("Warning mode activations: {}", warningModeActivations);
        log.info("Constraint: {}", runningAvgWait < 10.0 ? "PASSED" : "FAILED");

        return String.format("Processed %d orders - Avg wait: %.2f min (%s)",
                processed, runningAvgWait, runningAvgWait < 10.0 ? "PASSED" : "FAILED");
    }
}
