package com.example.coffee_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {

    // KPI Metrics
    private int totalOrders;
    private double avgWaitTime;
    private double weightedAvgWait;
    private double maxWaitTime;
    private double timeoutRate;
    private int timeoutCount;
    private double baristaUtilization;
    private int fairnessIssues;
    private int starvationCount;
    private int fifoSkips;
    private int completionInversions;
    private int complaintsRaised;

    // Validation
    private String validationStatus;
    private int violationsCount;
    private String avgWaitConstraint; // "PASSED" or "FAILED"
    private String failureReason; // Optional, only when FAILED

    // Distribution and Performance Data
    private List<DrinkDistribution> drinkDistribution;
    private List<BaristaPerformance> baristaPerformance;
    private List<TimeSlotPerformance> timeSlotPerformance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DrinkDistribution {
        private String drinkType;
        private int orderCount;
        private double percentage;
        private int prepTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaristaPerformance {
        private String testName;
        private double overallAvgWait;
        private double barista1Avg;
        private double barista2Avg;
        private double barista3Avg;
        private int complaints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotPerformance {
        private String timeSlot;
        private int customersArrived;
        private int ordersCompleted;
        private double avgWait;
        private double maxWait;
        private double timeoutPercent;
        private int fairnessViolations;
    }
}
