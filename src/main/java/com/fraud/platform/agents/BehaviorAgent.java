package com.fraud.platform.agents;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.AgentResult;
import com.fraud.platform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Behavioral analysis fraud detection agent.
 * Analyzes customer behavior patterns, timing anomalies, and transaction bursts.
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
    public AgentResult analyze(TransactionEvent transaction) {
        long startTime = System.currentTimeMillis();
        log.debug("BehaviorAgent analyzing transaction: {}", transaction.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Check for first-time customer (no transaction history)
        LocalDateTime thirtyDaysAgo = transaction.getTimestamp().minusDays(30);
        long historicalTxnCount = transactionRepository.countByCustomerIdAndTimestampBetween(
                transaction.getCustomerId(),
                thirtyDaysAgo,
                transaction.getTimestamp()
        );

        if (historicalTxnCount == 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(25));
            reasons.add("First transaction for customer");
        }

        // Check for unusual time (late night/early morning transactions)
        int hour = transaction.getTimestamp().getHour();
        if (hour >= 0 && hour < 6) {
            riskScore = riskScore.add(BigDecimal.valueOf(15));
            reasons.add("Transaction during unusual hours: " + hour + ":00");
        }

        // Check for weekend transactions (if unusual for customer)
        if (transaction.getTimestamp().getDayOfWeek().getValue() >= 6) {
            riskScore = riskScore.add(BigDecimal.valueOf(10));
            reasons.add("Weekend transaction");
        }

        // Check for rapid transaction bursts
        LocalDateTime burstStart = transaction.getTimestamp().minusMinutes(burstMinutes);
        long burstTxnCount = transactionRepository.countByCustomerIdAndTimestampBetween(
                transaction.getCustomerId(),
                burstStart,
                transaction.getTimestamp()
        );

        if (burstTxnCount >= burstCountThreshold * 2) {
            riskScore = riskScore.add(BigDecimal.valueOf(30));
            reasons.add("Rapid transaction burst: " + burstTxnCount + " transactions in " + burstMinutes + " minutes");
        } else if (burstTxnCount >= burstCountThreshold) {
            riskScore = riskScore.add(BigDecimal.valueOf(20));
            reasons.add("Elevated transaction frequency: " + burstTxnCount + " transactions in " + burstMinutes + " minutes");
        }

        // Check for unusual timing patterns (very late night - 2 AM to 5 AM)
        if (hour >= 2 && hour < 5) {
            riskScore = riskScore.add(BigDecimal.valueOf(20));
            reasons.add("Transaction during high-risk hours: " + hour + ":00 (2 AM - 5 AM)");
        }

        // Check for holiday transactions (simplified - check if it's a public holiday pattern)
        // In production, this would check against a holiday calendar
        int dayOfMonth = transaction.getTimestamp().getDayOfMonth();
        int month = transaction.getTimestamp().getMonthValue();
        if ((month == 1 && dayOfMonth == 1) || // New Year
            (month == 8 && dayOfMonth == 15) || // Independence Day
            (month == 10 && dayOfMonth == 2) || // Gandhi Jayanti
            (month == 12 && dayOfMonth == 25)) { // Christmas
            riskScore = riskScore.add(BigDecimal.valueOf(10));
            reasons.add("Transaction on public holiday");
        }

        // TODO: Add customer spending pattern analysis
        // TODO: Add merchant category deviation detection
        // TODO: Add amount deviation from customer baseline

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

        log.debug("BehaviorAgent completed: score={}, decision={}", riskScore, decision);
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