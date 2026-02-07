package com.example.coffee_shop.controller;

import com.example.coffee_shop.model.Order;
import com.example.coffee_shop.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(request.getCustomerName(), request.getDrinkId(), request.isLoyal());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/pickup")
    public ResponseEntity<Order> pickupOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.pickupOrder(id));
    }

    @Data
    public static class OrderRequest {
        private String customerName;
        private Long drinkId;
        private boolean isLoyal;
    }
}
