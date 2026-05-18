package com.fraud.platform.agents;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Risk-based fraud detection agent.
 * Analyzes transaction amount, merchant risk, payment type, customer history,
 * and fraud signals.
 * Leverages nested data structure for enhanced fraud detection.
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
            "CRYPTO", "GAMBLING", "FOREX", "CASINO", "BETTING");

    private static final List<String> HIGH_RISK_PAYMENT_TYPES = List.of(
            "CRYPTO", "WIRE_TRANSFER", "INTERNATIONAL");

    @Override
    public AgentResult analyze(CanonicalFraudEvent event) {
        long startTime = System.currentTimeMillis();
        log.debug("RiskAgent analyzing event: {}", event.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Use helper methods for safe access to nested data
        BigDecimal amount = event.getAmount();
        String customerId = event.getCustomerId();
        LocalDateTime timestamp = event.getTimestamp();

        // Check transaction amount
        if (amount != null) {
            if (amount.compareTo(highValueThreshold) >= 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(40));
                reasons.add("High-value transaction: " + amount);
            } else if (amount.compareTo(suspiciousValueThreshold) >= 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("Suspicious transaction amount: " + amount);
            }
        }
        /*
         * Customer spending deviation analysis
         */
        if (event.getCustomer() != null
                && event.getCustomer()
                        .getAvgTransactionAmount() != null) {

            BigDecimal avgAmount = event.getCustomer()
                    .getAvgTransactionAmount();

            if (avgAmount.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal multiplier = amount.divide(
                        avgAmount,
                        2,
                        RoundingMode.HALF_UP);

                /*
                 * Extremely abnormal transaction
                 */
                if (multiplier.compareTo(
                        BigDecimal.valueOf(10)) >= 0) {

                    riskScore = riskScore.add(
                            BigDecimal.valueOf(50));

                    reasons.add(
                            "Transaction amount exceeds 10x customer average");
                }

                /*
                 * Highly abnormal transaction
                 */
                else if (multiplier.compareTo(
                        BigDecimal.valueOf(5)) >= 0) {

                    riskScore = riskScore.add(
                            BigDecimal.valueOf(35));

                    reasons.add(
                            "Transaction amount exceeds 5x customer average");
                }

                /*
                 * Moderately abnormal transaction
                 */
                else if (multiplier.compareTo(
                        BigDecimal.valueOf(3)) >= 0) {

                    riskScore = riskScore.add(
                            BigDecimal.valueOf(20));

                    reasons.add(
                            "Transaction amount exceeds 3x customer average");
                }
            }
        }

        // Check merchant risk using nested merchant data
        if (event.getMerchantInfo() != null) {
            String merchantName = event.getMerchantInfo().getMerchantName();
            String merchantCategory = event.getMerchantInfo().getMerchantCategory();

            if (merchantName != null) {
                String merchant = merchantName.toUpperCase();
                for (String riskMerchant : HIGH_RISK_MERCHANTS) {
                    if (merchant.contains(riskMerchant)) {
                        riskScore = riskScore.add(BigDecimal.valueOf(30));
                        reasons.add("High-risk merchant category: " + riskMerchant);
                        break;
                    }
                }
            }

            // Additional check using merchant category field
            if (merchantCategory != null) {
                String category = merchantCategory.toUpperCase();
                for (String riskMerchant : HIGH_RISK_MERCHANTS) {
                    if (category.contains(riskMerchant)) {
                        riskScore = riskScore.add(BigDecimal.valueOf(30));
                        reasons.add("High-risk merchant category: " + riskMerchant);
                        break;
                    }
                }
            }
        }

        // Check payment type risk using nested transaction data
        if (event.getTransaction() != null && event.getTransaction().getPaymentType() != null) {
            String paymentType = event.getTransaction().getPaymentType().toUpperCase();
            // Check for crypto transactions
            if (paymentType.contains("CRYPTO")) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("Crypto transaction detected");
            }
            // Check for high-risk payment types
            for (String riskType : HIGH_RISK_PAYMENT_TYPES) {
                if (paymentType.contains(riskType)) {
                    riskScore = riskScore.add(BigDecimal.valueOf(20));
                    reasons.add("High-risk payment type: " + paymentType);
                    break;
                }
            }

        }

        // Check fraud signals for blacklisted merchant
        if (event.getFraudSignals() != null && Boolean.TRUE.equals(event.getFraudSignals().getBlacklistedMerchant())) {
            riskScore = riskScore.add(BigDecimal.valueOf(50));
            reasons.add("Merchant is blacklisted");
        }

        // Check behavior metrics for velocity
        if (event.getBehaviorMetrics() != null) {
            log.info("Behavior metrics runtime: {}", event.getBehaviorMetrics());
            Integer txnCount24h = event.getBehaviorMetrics().getTransactionCount24h();
            if (txnCount24h != null && txnCount24h > 20) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("High transaction velocity: " + txnCount24h + " transactions in 24h");
            } else if (txnCount24h != null && txnCount24h > 10) {
                riskScore = riskScore.add(BigDecimal.valueOf(15));
                reasons.add("Elevated transaction velocity: " + txnCount24h + " transactions in 24h");
            }
        }

        // Check abnormal transaction amount based on customer history
        if (customerId != null && timestamp != null && amount != null) {
            try {
                LocalDateTime historyStart = timestamp.minusDays(historyDays);
                BigDecimal avgAmount = transactionRepository.calculateAverageAmountByCustomerIdAndTimestampBetween(
                        customerId,
                        event.getTxnId(),
                        historyStart,
                        timestamp);

                if (avgAmount != null && avgAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Check if transaction amount is 10x the average
                    BigDecimal tenTimesAvg = avgAmount.multiply(BigDecimal.valueOf(10));
                    if (amount.compareTo(tenTimesAvg) >= 0) {
                        riskScore = riskScore.add(BigDecimal.valueOf(25));
                        reasons.add(String.format("Abnormal amount: %.2f is 10x customer average of %.2f",
                                amount, avgAmount));
                    }
                    // Check if transaction amount is 5x the average
                    else if (amount.compareTo(avgAmount.multiply(BigDecimal.valueOf(5))) >= 0) {
                        riskScore = riskScore.add(BigDecimal.valueOf(15));
                        reasons.add(String.format("Elevated amount: %.2f is 5x customer average of %.2f",
                                amount, avgAmount));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to calculate customer average amount for {}: {}",
                        customerId, e.getMessage());
            }
        }

        // Check customer average from nested customer data
        if (event.getCustomer() != null && event.getCustomer().getAvgTransactionAmount() != null && amount != null) {
            BigDecimal customerAvg = event.getCustomer().getAvgTransactionAmount();
            if (customerAvg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal fiveTimesAvg = customerAvg.multiply(BigDecimal.valueOf(5));
                if (amount.compareTo(fiveTimesAvg) >= 0) {
                    riskScore = riskScore.add(BigDecimal.valueOf(20));
                    reasons.add(String.format("Amount %.2f exceeds 5x customer baseline of %.2f",
                            amount, customerAvg));
                }
            }
        }

        // Cap risk score at 100
        if (riskScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            riskScore = BigDecimal.valueOf(100);
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

        log.info("RiskAgent completed for txn {}: score={}, decision={}, reasons={}",
                event.getTxnId(), riskScore, decision, reasons.size());
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