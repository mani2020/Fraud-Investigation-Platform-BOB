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
 * Risk-based fraud detection agent.
 * Analyzes transaction amount, merchant risk, payment type, and customer history.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RiskAgent implements FraudAgent {

    private final TransactionRepository transactionRepository;

    @Value("${fraud.thresholds.high-value:100000}")
    private BigDecimal highValueThreshold;

    @Value("${fraud.thresholds.suspicious-value:50000}")
    private BigDecimal suspiciousValueThreshold;

    @Value("${fraud.thresholds.history-days:30}")
    private int historyDays;

    private static final List<String> HIGH_RISK_MERCHANTS = List.of(
            "CRYPTO", "GAMBLING", "FOREX", "CASINO", "BETTING"
    );

    private static final List<String> HIGH_RISK_PAYMENT_TYPES = List.of(
            "CRYPTO", "WIRE_TRANSFER", "INTERNATIONAL"
    );

    @Override
    public AgentResult analyze(TransactionEvent transaction) {
        long startTime = System.currentTimeMillis();
        log.debug("RiskAgent analyzing transaction: {}", transaction.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Check transaction amount
        if (transaction.getAmount().compareTo(highValueThreshold) >= 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(40));
            reasons.add("High-value transaction: " + transaction.getAmount());
        } else if (transaction.getAmount().compareTo(suspiciousValueThreshold) >= 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(20));
            reasons.add("Suspicious transaction amount: " + transaction.getAmount());
        }

        // Check merchant risk
        String merchant = transaction.getMerchant().toUpperCase();
        for (String riskMerchant : HIGH_RISK_MERCHANTS) {
            if (merchant.contains(riskMerchant)) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("High-risk merchant category: " + riskMerchant);
                break;
            }
        }

        // Check payment type risk
        String paymentType = transaction.getPaymentType().toUpperCase();
        for (String riskType : HIGH_RISK_PAYMENT_TYPES) {
            if (paymentType.contains(riskType)) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("High-risk payment type: " + paymentType);
                break;
            }
        }

        // Check abnormal transaction amount based on customer history
        try {
            LocalDateTime historyStart = transaction.getTimestamp().minusDays(historyDays);
            BigDecimal avgAmount = transactionRepository.calculateAverageAmountByCustomerIdAndTimestampBetween(
                    transaction.getCustomerId(),
                    historyStart,
                    transaction.getTimestamp()
            );

            if (avgAmount != null && avgAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Check if transaction amount is 10x the average
                BigDecimal tenTimesAvg = avgAmount.multiply(BigDecimal.valueOf(10));
                if (transaction.getAmount().compareTo(tenTimesAvg) >= 0) {
                    riskScore = riskScore.add(BigDecimal.valueOf(25));
                    reasons.add(String.format("Abnormal amount: %.2f is 10x customer average of %.2f",
                            transaction.getAmount(), avgAmount));
                }
                // Check if transaction amount is 5x the average
                else if (transaction.getAmount().compareTo(avgAmount.multiply(BigDecimal.valueOf(5))) >= 0) {
                    riskScore = riskScore.add(BigDecimal.valueOf(15));
                    reasons.add(String.format("Elevated amount: %.2f is 5x customer average of %.2f",
                            transaction.getAmount(), avgAmount));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate customer average amount for {}: {}",
                    transaction.getCustomerId(), e.getMessage());
        }

        // Determine decision
        String decision;
        if (riskScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            decision = "REJECT";
        } else if (riskScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
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
                .confidence(BigDecimal.valueOf(0.85))
                .processingTimeMs(processingTime)
                .build();

        log.debug("RiskAgent completed: score={}, decision={}", riskScore, decision);
        return result;
    }

    @Override
    public String getAgentName() {
        return "RiskAgent";
    }

    @Override
    public double getWeight() {
        return 0.25; // 25% weight in final decision
    }
}

// Made with Bob