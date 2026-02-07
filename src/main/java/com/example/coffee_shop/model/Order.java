package com.example.coffee_shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coffee_orders") // 'order' is a reserved keyword in SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "drink_id", nullable = false)
    private Drink drink;

    @ManyToOne
    @JoinColumn(name = "barista_id")
    private Barista assignedBarista;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.WAITING;

    private double priorityScore = 0.0;

    private LocalDateTime orderTime;
    private LocalDateTime estimatedCompletionTime;
    private LocalDateTime completedTime;

    // For fairness logic: how many times was this order skipped?
    private int timesSkipped = 0;

    // For loyalty priority calculation
    private boolean isLoyal = false;

    // Hard deadline (10 mins from orderTime)
    private LocalDateTime hardDeadline;

    @PrePersist
    protected void onCreate() {
        if (this.orderTime == null) {
            this.orderTime = LocalDateTime.now();
        }
        if (this.hardDeadline == null) {
            this.hardDeadline = this.orderTime.plusMinutes(10);
        }
        if (this.status == null) {
            this.status = OrderStatus.WAITING;
        }
    }
}
