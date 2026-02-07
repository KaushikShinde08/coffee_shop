package com.example.coffee_shop.service;

import com.example.coffee_shop.model.Barista;
import com.example.coffee_shop.model.BaristaStatus;
import com.example.coffee_shop.model.Order;
import com.example.coffee_shop.model.OrderStatus;
import com.example.coffee_shop.repository.BaristaRepository;
import com.example.coffee_shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaristaScheduler {

    private final OrderRepository orderRepository;
    private final BaristaRepository baristaRepository;
    private final PriorityService priorityService;

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void processQueue() {
        log.info("Running BaristaScheduler...");

        // 0. CLEANUP: Fix any stuck PREPARING orders from legacy data or bugs
        cleanupStuckOrders();

        // 1. Check for orders that are done preparing
        checkReadyOrders();

        // 2. Recalculate priorities for all WAITING orders
        List<Order> waitingOrders = orderRepository.findByStatus(OrderStatus.WAITING);

        for (Order order : waitingOrders) {
            double newScore = priorityService.calculatePriority(order);
            order.setPriorityScore(newScore);
        }
        orderRepository.saveAll(waitingOrders);

        // 3. Assign orders to available baristas
        // CRITICAL: Check current PREPARING count BEFORE assignment
        List<Order> preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);
        int currentPreparingCount = preparingOrders.size();

        // HARD CONSTRAINT: Max 3 orders can be PREPARING at once (1 per barista)
        if (currentPreparingCount >= 3) {
            log.info("Max barista capacity reached ({}/3 preparing). No new assignments.", currentPreparingCount);
            return;
        }

        List<Barista> activeBaristas = baristaRepository.findByStatus(BaristaStatus.ACTIVE);

        // Fetch orders sorted by priority (Highest first)
        List<Order> sortedOrders = orderRepository.findByStatusOrderByPriorityScoreDesc(OrderStatus.WAITING);

        if (sortedOrders.isEmpty() || activeBaristas.isEmpty()) {
            return;
        }

        // Find which baristas are currently assigned to PREPARING orders
        List<Long> busyBaristaIds = preparingOrders.stream()
                .map(o -> o.getAssignedBarista() != null ? o.getAssignedBarista().getId() : null)
                .filter(id -> id != null)
                .toList();

        // Only assign to baristas that are NOT currently working on a PREPARING order
        List<Barista> freeBaristas = activeBaristas.stream()
                .filter(b -> !busyBaristaIds.contains(b.getId()))
                .toList();

        // Calculate how many new orders we can assign
        int maxNewAssignments = 3 - currentPreparingCount;

        log.info("Barista Status: {}/3 preparing, {} free baristas, max {} new assignments",
                currentPreparingCount, freeBaristas.size(), maxNewAssignments);

        // Assign orders to free baristas up to the limit
        for (int i = 0; i < Math.min(maxNewAssignments, Math.min(freeBaristas.size(), sortedOrders.size())); i++) {
            Barista barista = freeBaristas.get(i);
            Order orderToAssign = sortedOrders.get(i);

            assignOrderToBarista(orderToAssign, barista);
        }
    }

    private void checkReadyOrders() {
        // Find all orders that are PREPARING
        // Since we don't have a direct repository method for PREPARING, we might need
        // to add it or fetch all and filter.
        // Better to add findByStatus to repository if not strictly just WAITING.
        // Actually OrderRepository likely has findByStatus which is generic.
        List<Order> preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);
        LocalDateTime now = LocalDateTime.now();

        for (Order order : preparingOrders) {
            if (order.getEstimatedCompletionTime() != null && order.getEstimatedCompletionTime().isBefore(now)) {
                log.info("Order {} is ready for pickup!", order.getId());
                order.setStatus(OrderStatus.READY_TO_PICKUP);
                // Reduce load on barista?
                // Ideally yes, but our simple model increments load at assignment.
                // We could decrement it here, or just let the scheduler reset loads
                // periodically.
                // For this simple logic, let's assume load clears over time or we decrement it.
                if (order.getAssignedBarista() != null) {
                    Barista b = order.getAssignedBarista();
                    b.setCurrentLoadMinutes(
                            Math.max(0, b.getCurrentLoadMinutes() - order.getDrink().getPrepTimeMinutes()));
                    baristaRepository.save(b);
                }
                orderRepository.save(order);
            }
        }
    }

    /**
     * CRITICAL CLEANUP: Enforce hard constraint that max 3 orders can be PREPARING
     * Detects and fixes:
     * 1. Orders in PREPARING without a barista assigned
     * 2. Excess PREPARING orders beyond the 3-barista limit
     * 3. Multiple orders assigned to the same barista
     */
    private void cleanupStuckOrders() {
        List<Order> preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);

        if (preparingOrders.isEmpty()) {
            return;
        }

        int ordersReset = 0;

        // Step 1: Reset orders without a valid barista assignment
        for (Order order : preparingOrders) {
            if (order.getAssignedBarista() == null) {
                log.warn("Order {} in PREPARING but no barista assigned. Resetting to WAITING.", order.getId());
                order.setStatus(OrderStatus.WAITING);
                order.setEstimatedCompletionTime(null);
                orderRepository.save(order);
                ordersReset++;
            }
        }

        // Step 2: If more than 3 orders in PREPARING, reset the excess (keep oldest 3)
        preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);
        if (preparingOrders.size() > 3) {
            log.warn("CONSTRAINT VIOLATION: {} orders in PREPARING (max 3). Resetting excess orders to WAITING.",
                    preparingOrders.size());

            // Sort by order time (oldest first) to keep the 3 oldest
            preparingOrders.sort((o1, o2) -> o1.getOrderTime().compareTo(o2.getOrderTime()));

            // Reset all orders beyond the first 3
            for (int i = 3; i < preparingOrders.size(); i++) {
                Order order = preparingOrders.get(i);
                log.info("Resetting Order {} (assigned to {}) back to WAITING",
                        order.getId(),
                        order.getAssignedBarista() != null ? order.getAssignedBarista().getName() : "none");

                order.setStatus(OrderStatus.WAITING);
                order.setAssignedBarista(null);
                order.setEstimatedCompletionTime(null);
                orderRepository.save(order);
                ordersReset++;
            }
        }

        // Step 3: Check for baristas with multiple PREPARING orders (violation of
        // 1-order-per-barista rule)
        preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);
        Map<Long, List<Order>> ordersByBarista = preparingOrders.stream()
                .filter(o -> o.getAssignedBarista() != null)
                .collect(Collectors.groupingBy(o -> o.getAssignedBarista().getId()));

        for (Map.Entry<Long, List<Order>> entry : ordersByBarista.entrySet()) {
            if (entry.getValue().size() > 1) {
                log.warn("Barista ID {} has {} orders in PREPARING (max 1). Resetting extras to WAITING.",
                        entry.getKey(), entry.getValue().size());

                // Keep the first order, reset the rest
                List<Order> baristaOrders = entry.getValue();
                for (int i = 1; i < baristaOrders.size(); i++) {
                    Order order = baristaOrders.get(i);
                    order.setStatus(OrderStatus.WAITING);
                    order.setAssignedBarista(null);
                    order.setEstimatedCompletionTime(null);
                    orderRepository.save(order);
                    ordersReset++;
                }
            }
        }

        // Reset barista loads to match actual PREPARING orders
        List<Barista> allBaristas = baristaRepository.findByStatus(BaristaStatus.ACTIVE);
        for (Barista barista : allBaristas) {
            barista.setCurrentLoadMinutes(0);
            baristaRepository.save(barista);
        }

        if (ordersReset > 0) {
            log.info("Cleanup complete: {} orders reset to WAITING to enforce concurrency constraint", ordersReset);
        }
    }

    private void assignOrderToBarista(Order order, Barista barista) {
        order.setAssignedBarista(barista);
        order.setStatus(OrderStatus.PREPARING);
        order.setEstimatedCompletionTime(LocalDateTime.now().plusMinutes(order.getDrink().getPrepTimeMinutes()));

        barista.setCurrentLoadMinutes(barista.getCurrentLoadMinutes() + order.getDrink().getPrepTimeMinutes());

        orderRepository.save(order);
        baristaRepository.save(barista);

        log.info("Assigned Order {} to Barista {}", order.getId(), barista.getName());
    }
}
