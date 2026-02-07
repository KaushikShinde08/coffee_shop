package com.example.coffee_shop.service;

import com.example.coffee_shop.dto.StatsDTO;
import com.example.coffee_shop.model.Order;
import com.example.coffee_shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

        private final OrderRepository orderRepository;

        public StatsDTO calculateStatistics() {
                // Get all orders from database
                List<Order> allOrders = orderRepository.findAll();

                // Filter completed orders for accurate statistics
                List<Order> completedOrders = allOrders.stream()
                                .filter(o -> o.getCompletedTime() != null)
                                .toList();

                if (completedOrders.isEmpty()) {
                        return createEmptyStats();
                }

                // Calculate KPI metrics
                int totalOrders = completedOrders.size();
                double avgWaitTime = calculateAvgWaitTime(completedOrders);
                double weightedAvgWait = calculateWeightedAvgWait(completedOrders);
                double maxWaitTime = calculateMaxWaitTime(completedOrders);

                // Timeout analysis
                long timeoutCount = completedOrders.stream()
                                .filter(o -> getWaitTime(o) > 10.0)
                                .count();
                double timeoutRate = (timeoutCount * 100.0) / totalOrders;

                // Barista utilization (estimate based on completed orders and time span)
                double baristaUtilization = calculateBaristaUtilization(completedOrders);

                // Fairness metrics
                int fairnessIssues = calculateFairnessIssues(completedOrders);
                int starvationCount = (int) completedOrders.stream()
                                .filter(o -> o.getTimesSkipped() > 3)
                                .count();
                int fifoSkips = calculateFifoSkips(completedOrders);
                int completionInversions = calculateCompletionInversions(completedOrders);
                int complaintsRaised = (int) timeoutCount; // Complaints = timeouts

                // Validation status
                String validationStatus = timeoutCount > 0 ? "warning" : "passed";
                int violationsCount = (int) timeoutCount;
                String avgWaitConstraint = avgWaitTime < 10.0 ? "PASSED" : "FAILED";
                String failureReason = avgWaitTime >= 10.0
                                ? String.format("Average wait time (%.2f min) exceeded 10 minutes", avgWaitTime)
                                : null;

                // Distribution and performance data
                List<StatsDTO.DrinkDistribution> drinkDistribution = calculateDrinkDistribution(completedOrders);
                List<StatsDTO.BaristaPerformance> baristaPerformance = calculateBaristaPerformance(completedOrders);
                List<StatsDTO.TimeSlotPerformance> timeSlotPerformance = calculateTimeSlotPerformance(completedOrders);

                return StatsDTO.builder()
                                .totalOrders(totalOrders)
                                .avgWaitTime(avgWaitTime)
                                .weightedAvgWait(weightedAvgWait)
                                .maxWaitTime(maxWaitTime)
                                .timeoutRate(timeoutRate)
                                .timeoutCount((int) timeoutCount)
                                .baristaUtilization(baristaUtilization)
                                .fairnessIssues(fairnessIssues)
                                .starvationCount(starvationCount)
                                .fifoSkips(fifoSkips)
                                .completionInversions(completionInversions)
                                .complaintsRaised(complaintsRaised)
                                .validationStatus(validationStatus)
                                .violationsCount(violationsCount)
                                .avgWaitConstraint(avgWaitConstraint)
                                .failureReason(failureReason)
                                .drinkDistribution(drinkDistribution)
                                .baristaPerformance(baristaPerformance)
                                .timeSlotPerformance(timeSlotPerformance)
                                .build();
        }

        private double getWaitTime(Order order) {
                if (order.getOrderTime() == null || order.getCompletedTime() == null) {
                        return 0.0;
                }
                return Duration.between(order.getOrderTime(), order.getCompletedTime()).toMinutes();
        }

        private double calculateAvgWaitTime(List<Order> orders) {
                return orders.stream()
                                .mapToDouble(this::getWaitTime)
                                .average()
                                .orElse(0.0);
        }

        private double calculateWeightedAvgWait(List<Order> orders) {
                // Weight loyal customers more heavily
                double totalWeightedWait = orders.stream()
                                .mapToDouble(o -> getWaitTime(o) * (o.isLoyal() ? 1.5 : 1.0))
                                .sum();
                double totalWeight = orders.stream()
                                .mapToDouble(o -> o.isLoyal() ? 1.5 : 1.0)
                                .sum();
                return totalWeight > 0 ? totalWeightedWait / totalWeight : 0.0;
        }

        private double calculateMaxWaitTime(List<Order> orders) {
                return orders.stream()
                                .mapToDouble(this::getWaitTime)
                                .max()
                                .orElse(0.0);
        }

        private double calculateBaristaUtilization(List<Order> orders) {
                if (orders.isEmpty())
                        return 0.0;

                // Total prep time / (3 baristas * time span)
                double totalPrepTime = orders.stream()
                                .mapToDouble(o -> o.getDrink().getPrepTimeMinutes())
                                .sum();

                LocalDateTime earliest = orders.stream()
                                .map(Order::getOrderTime)
                                .min(LocalDateTime::compareTo)
                                .orElse(LocalDateTime.now());
                LocalDateTime latest = orders.stream()
                                .map(Order::getCompletedTime)
                                .max(LocalDateTime::compareTo)
                                .orElse(LocalDateTime.now());

                double timeSpanMinutes = Duration.between(earliest, latest).toMinutes();
                if (timeSpanMinutes == 0)
                        return 0.0;

                return Math.min(100.0, (totalPrepTime / (3 * timeSpanMinutes)) * 100);
        }

        private int calculateFairnessIssues(List<Order> orders) {
                return (int) orders.stream()
                                .filter(o -> o.getTimesSkipped() > 0)
                                .count();
        }

        private int calculateFifoSkips(List<Order> orders) {
                // Count orders completed out of FIFO order
                List<Order> sortedByOrder = orders.stream()
                                .sorted(Comparator.comparing(Order::getOrderTime))
                                .toList();
                List<Order> sortedByCompletion = orders.stream()
                                .sorted(Comparator.comparing(Order::getCompletedTime))
                                .toList();

                int skips = 0;
                for (int i = 0; i < sortedByOrder.size(); i++) {
                        if (!sortedByOrder.get(i).equals(sortedByCompletion.get(i))) {
                                skips++;
                        }
                }
                return skips;
        }

        private int calculateCompletionInversions(List<Order> orders) {
                // Count pairs where later order completed before earlier order
                List<Order> sorted = orders.stream()
                                .sorted(Comparator.comparing(Order::getOrderTime))
                                .toList();

                int inversions = 0;
                for (int i = 0; i < sorted.size() - 1; i++) {
                        for (int j = i + 1; j < sorted.size(); j++) {
                                if (sorted.get(i).getCompletedTime().isAfter(sorted.get(j).getCompletedTime())) {
                                        inversions++;
                                }
                        }
                }
                return inversions;
        }

        private List<StatsDTO.DrinkDistribution> calculateDrinkDistribution(List<Order> orders) {
                Map<String, Long> drinkCounts = orders.stream()
                                .collect(Collectors.groupingBy(
                                                o -> o.getDrink().getName(),
                                                Collectors.counting()));

                int total = orders.size();
                return drinkCounts.entrySet().stream()
                                .map(e -> {
                                        Order sampleOrder = orders.stream()
                                                        .filter(o -> o.getDrink().getName().equals(e.getKey()))
                                                        .findFirst()
                                                        .orElse(null);

                                        return StatsDTO.DrinkDistribution.builder()
                                                        .drinkType(e.getKey())
                                                        .orderCount(e.getValue().intValue())
                                                        .percentage((e.getValue() * 100.0) / total)
                                                        .prepTime(sampleOrder != null
                                                                        ? sampleOrder.getDrink().getPrepTimeMinutes()
                                                                        : 0)
                                                        .build();
                                })
                                .sorted(Comparator.comparing(StatsDTO.DrinkDistribution::getOrderCount).reversed())
                                .toList();
        }

        private List<StatsDTO.BaristaPerformance> calculateBaristaPerformance(List<Order> orders) {
                // Group by barista
                Map<String, List<Order>> ordersByBarista = orders.stream()
                                .filter(o -> o.getAssignedBarista() != null)
                                .collect(Collectors.groupingBy(o -> o.getAssignedBarista().getName()));

                double overallAvg = calculateAvgWaitTime(orders);
                long complaints = orders.stream().filter(o -> getWaitTime(o) > 10.0).count();

                return List.of(StatsDTO.BaristaPerformance.builder()
                                .testName("Current Simulation")
                                .overallAvgWait(overallAvg)
                                .barista1Avg(ordersByBarista.getOrDefault("Alice", Collections.emptyList()).stream()
                                                .mapToDouble(this::getWaitTime).average().orElse(0.0))
                                .barista2Avg(ordersByBarista.getOrDefault("Bob", Collections.emptyList()).stream()
                                                .mapToDouble(this::getWaitTime).average().orElse(0.0))
                                .barista3Avg(ordersByBarista.getOrDefault("Charlie", Collections.emptyList()).stream()
                                                .mapToDouble(this::getWaitTime).average().orElse(0.0))
                                .complaints((int) complaints)
                                .build());
        }

        private List<StatsDTO.TimeSlotPerformance> calculateTimeSlotPerformance(List<Order> orders) {
                // Group by 30-minute time slots
                Map<String, List<Order>> ordersBySlot = orders.stream()
                                .collect(Collectors.groupingBy(o -> getTimeSlot(o.getOrderTime())));

                return ordersBySlot.entrySet().stream()
                                .map(e -> {
                                        List<Order> slotOrders = e.getValue();
                                        long timeouts = slotOrders.stream().filter(o -> getWaitTime(o) > 10.0).count();
                                        double timeoutPercent = (timeouts * 100.0) / slotOrders.size();

                                        return StatsDTO.TimeSlotPerformance.builder()
                                                        .timeSlot(e.getKey())
                                                        .customersArrived(slotOrders.size())
                                                        .ordersCompleted(slotOrders.size())
                                                        .avgWait(slotOrders.stream().mapToDouble(this::getWaitTime)
                                                                        .average().orElse(0.0))
                                                        .maxWait(slotOrders.stream().mapToDouble(this::getWaitTime)
                                                                        .max().orElse(0.0))
                                                        .timeoutPercent(timeoutPercent)
                                                        .fairnessViolations((int) slotOrders.stream()
                                                                        .filter(o -> o.getTimesSkipped() > 0).count())
                                                        .build();
                                })
                                .sorted(Comparator.comparing(StatsDTO.TimeSlotPerformance::getTimeSlot))
                                .toList();
        }

        private String getTimeSlot(LocalDateTime dateTime) {
                LocalTime time = dateTime.toLocalTime();
                int hour = time.getHour();
                int minute = time.getMinute();

                // Round to 30-minute slots
                int slotMinute = (minute < 30) ? 0 : 30;
                LocalTime slotStart = LocalTime.of(hour, slotMinute);
                LocalTime slotEnd = slotStart.plusMinutes(30);

                return slotStart.toString() + "-" + slotEnd.toString();
        }

        private StatsDTO createEmptyStats() {
                return StatsDTO.builder()
                                .totalOrders(0)
                                .avgWaitTime(0.0)
                                .weightedAvgWait(0.0)
                                .maxWaitTime(0.0)
                                .timeoutRate(0.0)
                                .timeoutCount(0)
                                .baristaUtilization(0.0)
                                .fairnessIssues(0)
                                .starvationCount(0)
                                .fifoSkips(0)
                                .completionInversions(0)
                                .complaintsRaised(0)
                                .validationStatus("no_data")
                                .violationsCount(0)
                                .drinkDistribution(Collections.emptyList())
                                .baristaPerformance(Collections.emptyList())
                                .timeSlotPerformance(Collections.emptyList())
                                .build();
        }
}
