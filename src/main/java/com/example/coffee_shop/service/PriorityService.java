package com.example.coffee_shop.service;

import com.example.coffee_shop.model.Order;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PriorityService {

    // Weights
    private static final double WEIGHT_WAIT_TIME = 0.40;
    private static final double WEIGHT_COMPLEXITY = 0.25;
    private static final double WEIGHT_URGENCY = 0.25;
    private static final double WEIGHT_LOYALTY = 0.10;

    /**
     * Calculates the priority score for an order.
     * Higher score = Higher priority.
     */
    public double calculatePriority(Order order) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Waiting Time Factor
        // Normalize: Let's say 10 minutes wait is max "normal" wait -> score 1.0
        // Minutes waiting / 10.0
        double minutesWaiting = Duration.between(order.getOrderTime(), now).toSeconds() / 60.0;
        // Component 1: Wait Time (40%)
        // Impact: Increases over time.
        // Let's say max tolerated wait is 10 mins.
        // Score = (MinutesWaiting / 10.0) * 100
        double waitScoreRaw = (minutesWaiting / 10.0) * 100;

        // Component 2: Complexity/Prep Time (25%)
        // Impact: Short orders = High Priority.
        // Max prep time is around 6 mins (Mocha). Min is 1 min.
        // Formula: (MaxPrep - ActualPrep) / MaxPrep
        // Let's use 10 mins as a theoretical max baseline to be safe? Or just 6.
        double maxPrep = 6.0;
        double prepTime = (double) order.getDrink().getPrepTimeMinutes();
        double complexityScoreRaw = Math.max(0, (maxPrep - prepTime + 1) / maxPrep) * 100;
        // +1 to ensure even the longest drink gets some small positive or at least 0.
        // Actually simple inversion: 1 min -> 100, 6 min -> 0?
        // Let's say: 1/PrepTime * constant?
        // Linearly mapping 1..6 to 100..0
        // 1 -> 100, 6 -> 0.
        // Slope = -20. Intercept = 120. Score = 120 - 20*Prep.
        // Check: 1 min -> 100. 6 min -> 0. Correct.
        complexityScoreRaw = Math.max(0, 120 - (20 * prepTime));

        // Component 3: Urgency (25%)
        // Impact: Close to 10 min deadline = High Priority.
        // TimeLeft = Deadline - Now.
        // If TimeLeft < 2 mins, boost significantly?
        // Or just linear: (10 - TimeLeft) / 10.
        // Which is exactly the same as Wait Time physically (since Deadline is fixed
        // offset).
        // BUT the prompt distinguishes them.
        // "Urgency: Enforce timeout".
        // Maybe this kicks in exponentially as we approach 10 mins?
        // Let's use a linear scale for now, similar to wait time but conceptually
        // different in the weight mix.
        // Actually, if Deadline is purely WaitTime + 10, then Urgency is perfectly
        // correlated with WaitTime.
        // UNLESS the deadline is dynamic? No, "Hard 10-minute maximum wait".
        // So mathematically, WaitTime and Urgency are collinear.
        // However, we can treat "Urgency" as a non-linear boost logic.
        // e.g. if wait > 8 mins, Urgency spikes.
        double urgencyScoreRaw = 0;
        if (minutesWaiting > 8.0) {
            urgencyScoreRaw = 100.0; // Emergency boost
        } else {
            urgencyScoreRaw = (minutesWaiting / 8.0) * 50.0; // Gradual increase
        }

        // Component 4: Loyalty (10%)
        double loyaltyScoreRaw = order.isLoyal() ? 100.0 : 0.0;

        // Final Weighted Sum
        double finalScore = (waitScoreRaw * WEIGHT_WAIT_TIME) +
                (complexityScoreRaw * WEIGHT_COMPLEXITY) +
                (urgencyScoreRaw * WEIGHT_URGENCY) +
                (loyaltyScoreRaw * WEIGHT_LOYALTY);

        return finalScore;
    }

    /**
     * Calculate priority with throughput protection - biases short jobs during
     * congestion
     */
    public double calculatePriorityWithThroughput(Order order, double currentAvgWait) {
        double basePriority = calculatePriority(order);

        // Throughput bonus based on prep time and congestion level
        double throughputBonus = 0.0;
        int prepTime = order.getDrink().getPrepTimeMinutes();

        if (currentAvgWait >= 7.5) {
            // CONGESTED: Aggressive throughput bias
            if (prepTime <= 2) {
                throughputBonus = 20.0; // Major boost for fast drinks
            } else if (prepTime == 4) {
                throughputBonus = 5.0; // Small boost for medium
            } else { // 6+ min
                throughputBonus = -15.0; // Penalize long drinks
            }
        } else if (currentAvgWait >= 5.0) {
            // MODERATE: Light throughput bias
            if (prepTime <= 2) {
                throughputBonus = 10.0;
            } else if (prepTime >= 6) {
                throughputBonus = -5.0;
            }
        } else {
            // NORMAL: Minimal bias
            if (prepTime <= 1) {
                throughputBonus = 5.0;
            }
        }

        return basePriority + throughputBonus;
    }
}
