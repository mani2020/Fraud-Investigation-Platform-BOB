package com.fraud.platform.agents;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Behavioral analysis fraud detection agent.
 * Analyzes customer behavior patterns, timing anomalies, transaction velocity,
 * and amount deviations.
 * Leverages nested behavior metrics and customer data for enhanced detection.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BehaviorAgent implements FraudAgent {

    private final TransactionRepository transactionRepository;

    @Value("${fraud.thresholds.burst-count:3}")
    private int burstCountThreshold;

    @Value("${fraud.thresholds.burst-minutes:5}")
    private int burstMinutes;

    @Override
    public AgentResult analyze(CanonicalFraudEvent event) {
        long startTime = System.currentTimeMillis();
        log.debug("BehaviorAgent analyzing event: {}", event.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Use helper methods for safe access
        String customerId = event.getCustomerId();
        LocalDateTime timestamp = event.getTimestamp();
        BigDecimal amount = event.getAmount();

        // Check behavior metrics first (if available)
        if (event.getBehaviorMetrics() != null) {
            log.info(  "Behavior metrics runtime: {}",   event.getBehaviorMetrics());
            Integer txnCount24h = event.getBehaviorMetrics().getTransactionCount24h();
            BigDecimal totalAmount24h = event.getBehaviorMetrics().getTotalAmount24h();

            // Check transaction velocity
            if (txnCount24h != null) {
                if (txnCount24h > 20) {
                    riskScore = riskScore.add(BigDecimal.valueOf(35));
                    reasons.add("Very high transaction velocity: " + txnCount24h + " in 24h");
                } else if (txnCount24h > 10) {
                    riskScore = riskScore.add(BigDecimal.valueOf(20));
                    reasons.add("High transaction velocity: " + txnCount24h + " in 24h");
                }
            }

            // Check amount velocity
            if (totalAmount24h != null && totalAmount24h.compareTo(BigDecimal.valueOf(100000)) >= 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("High transaction volume in 24h: " + totalAmount24h);
            }

            // Check for unusual time
            if (Boolean.TRUE.equals(event.getBehaviorMetrics().getUnusualTime())) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("Transaction at unusual time detected");
            }

            // Check for unusual amount
            if (Boolean.TRUE.equals(event.getBehaviorMetrics().getUnusualAmount())) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("Unusual transaction amount detected");
            }

            // Check for unusual location
            if (Boolean.TRUE.equals(event.getBehaviorMetrics().getUnusualLocation())) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("Transaction from unusual location");
            }
        }

        // Check customer baseline from nested customer data
        if (event.getCustomer() != null && amount != null) {
            BigDecimal avgAmount = event.getCustomer().getAvgTransactionAmount();
            if (avgAmount != null && avgAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Check if amount is significantly higher than customer average
                BigDecimal threeTimesAvg = avgAmount.multiply(BigDecimal.valueOf(3));
                if (amount.compareTo(threeTimesAvg) >= 0) {
                    riskScore = riskScore.add(BigDecimal.valueOf(25));
                    reasons.add(String.format("Amount %.2f is 3x customer average of %.2f",
                            amount, avgAmount));
                }
            }
        }

        // Fallback to repository checks if behavior metrics not available
        if (customerId != null && timestamp != null) {
            // Check for first-time customer (no transaction history)
            LocalDateTime thirtyDaysAgo = timestamp.minusDays(30);
            long historicalTxnCount = transactionRepository.countByCustomerIdAndTimestampBetween(
                    customerId,
                    event.getTxnId(),
                    thirtyDaysAgo,
                    timestamp);

            if (historicalTxnCount == 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("First transaction for customer");
            }

            // Check for rapid transaction bursts
            LocalDateTime burstStart = timestamp.minusMinutes(burstMinutes);
            long burstTxnCount = transactionRepository.countByCustomerIdAndTimestampBetween(
                    customerId,
                    event.getTxnId(),
                    burstStart,
                    timestamp);

            if (burstTxnCount >= burstCountThreshold * 2) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add(
                        "Rapid transaction burst: " + burstTxnCount + " transactions in " + burstMinutes + " minutes");
            } else if (burstTxnCount >= burstCountThreshold) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("Elevated transaction frequency: " + burstTxnCount + " transactions in " + burstMinutes
                        + " minutes");
            }
        }

        // Check timing patterns
        if (timestamp != null) {
            int hour = timestamp.getHour();

            // Check for unusual time (late night/early morning transactions)
            if (hour >= 0 && hour < 6) {
                riskScore = riskScore.add(BigDecimal.valueOf(15));
                reasons.add("Transaction during unusual hours: " + hour + ":00");
            }

            // Check for unusual timing patterns (very late night - 2 AM to 5 AM)
            if (hour >= 2 && hour < 5) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("Transaction during high-risk hours: " + hour + ":00 (2 AM - 5 AM)");
            }

            // Check for weekend transactions (if unusual for customer)
            if (timestamp.getDayOfWeek().getValue() >= 6) {
                riskScore = riskScore.add(BigDecimal.valueOf(10));
                reasons.add("Weekend transaction");
            }

            // Check for holiday transactions (simplified - check if it's a public holiday
            // pattern)
            int dayOfMonth = timestamp.getDayOfMonth();
            int month = timestamp.getMonthValue();
            if ((month == 1 && dayOfMonth == 1) || // New Year
                    (month == 8 && dayOfMonth == 15) || // Independence Day
                    (month == 10 && dayOfMonth == 2) || // Gandhi Jayanti
                    (month == 12 && dayOfMonth == 25)) { // Christmas
                riskScore = riskScore.add(BigDecimal.valueOf(10));
                reasons.add("Transaction on public holiday");
            }
        }

        // Cap risk score at 100
        if (riskScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            riskScore = BigDecimal.valueOf(100);
        }

        // Determine decision
        String decision;
        if (riskScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            decision = "REJECT";
        } else if (riskScore.compareTo(BigDecimal.valueOf(30)) >= 0) {
            decision = "REVIEW";
        } else {
            decision = "APPROVE";
        }

        long processingTime = System.currentTimeMillis() - startTime;

        AgentResult result = AgentResult.builder()
                .agentName(getAgentName())
                .riskScore(riskScore)
                .decision(decision)
                .reasons(reasons)
                .confidence(BigDecimal.valueOf(0.70))
                .processingTimeMs(processingTime)
                .build();

        log.info("BehaviorAgent completed for txn {}: score={}, decision={}, reasons={}",
                event.getTxnId(), riskScore, decision, reasons.size());
        return result;
    }

    @Override
    public String getAgentName() {
        return "BehaviorAgent";
    }

    @Override
    public double getWeight() {
        return 0.10; // 10% weight in final decision
    }
}

// Made with Bob