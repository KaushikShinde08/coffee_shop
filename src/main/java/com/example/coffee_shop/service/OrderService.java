package com.example.coffee_shop.service;

import com.example.coffee_shop.model.Drink;
import com.example.coffee_shop.model.Order;
import com.example.coffee_shop.model.OrderStatus;
import com.example.coffee_shop.repository.DrinkRepository;
import com.example.coffee_shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DrinkRepository drinkRepository;
    private final PriorityService priorityService;

    @Transactional
    public Order placeOrder(String customerName, Long drinkId, boolean isLoyal) {
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new IllegalArgumentException("Drink not found with ID: " + drinkId));

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setDrink(drink);
        order.setLoyal(isLoyal);
        order.setStatus(OrderStatus.WAITING);
        order.setOrderTime(LocalDateTime.now());
        order.setHardDeadline(order.getOrderTime().plusMinutes(10));

        // Initial priority calculation (Wait time is 0, but complexity/loyalty matter)
        double initialPriority = priorityService.calculatePriority(order);
        order.setPriorityScore(initialPriority);

        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order pickupOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.READY_TO_PICKUP) {
            throw new IllegalStateException("Order is not ready for pickup. Current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
}
