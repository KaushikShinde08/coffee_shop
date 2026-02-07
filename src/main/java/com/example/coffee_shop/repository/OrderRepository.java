package com.example.coffee_shop.repository;

import com.example.coffee_shop.model.Order;
import com.example.coffee_shop.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusOrderByPriorityScoreDesc(OrderStatus status);
}
