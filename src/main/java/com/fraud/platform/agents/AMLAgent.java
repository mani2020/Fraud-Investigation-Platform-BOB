package com.fraud.platform.agents;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.model.nested.MerchantInfo;
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
 * Anti-Money Laundering (AML) fraud detection agent.
 * Analyzes transaction patterns, merchant categories, customer risk profiles, and velocity for money laundering indicators.
 * Leverages nested data structure and fraud signals for enhanced AML detection.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AMLAgent implements FraudAgent {

    private final TransactionRepository transactionRepository;

    @Value("${fraud.thresholds.velocity-count:5}")
    private int velocityCountThreshold;

    @Value("${fraud.thresholds.velocity-minutes:10}")
    private int velocityMinutes;

    @Value("${fraud.thresholds.daily-amount:500000}")
    private BigDecimal dailyAmountThreshold;

    // High-risk merchants for money laundering (including crypto)
    private static final List<String> HIGH_RISK_AML_MERCHANTS = List.of(
            "CRYPTO", "CRYPTOCURRENCY", "BITCOIN", "ETHEREUM", "BLOCKCHAIN",
            "EXCHANGE", "CASINO", "GAMBLING", "BETTING",
            "MONEY_TRANSFER", "REMITTANCE", "FOREX", "GOLD", "JEWELRY",
            "PAWN", "AUCTION", "DEALER", "BROKER", "NFT", "DEFI"
    );

    // Cash-intensive businesses (higher AML risk)
    private static final List<String> CASH_INTENSIVE_MERCHANTS = List.of(
            "ATM", "CASH", "CHECK_CASHING", "PAYDAY", "LOAN",
            "BAR", "NIGHTCLUB", "LIQUOR", "TOBACCO", "VAPE"
    );

    @Override
    public AgentResult analyze(CanonicalFraudEvent event) {
        long startTime = System.currentTimeMillis();
        log.debug("AMLAgent analyzing event: {}", event.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Use helper methods for safe access
        BigDecimal amount = event.getAmount();
        String customerId = event.getCustomerId();
        LocalDateTime timestamp = event.getTimestamp();

        // Check merchant using nested data
        String merchant = null;
        String merchantCategory = null;
        if (event.getMerchantInfo() != null) {
            merchant = event.getMerchantInfo().getMerchantName();
            merchantCategory = event.getMerchantInfo().getMerchantCategory();
        }

        // Check velocity using behavior metrics first (if available)
        if (event.getBehaviorMetrics() != null) {
            Integer txnCount24h = event.getBehaviorMetrics().getTransactionCount24h();
            BigDecimal totalAmount24h = event.getBehaviorMetrics().getTotalAmount24h();
            
            if (txnCount24h != null && txnCount24h >= velocityCountThreshold * 2) {
                riskScore = riskScore.add(BigDecimal.valueOf(45));
                reasons.add("Very high velocity: " + txnCount24h + " transactions in 24h");
            } else if (txnCount24h != null && txnCount24h >= velocityCountThreshold) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("High velocity: " + txnCount24h + " transactions in 24h");
            }
            
            if (totalAmount24h != null && totalAmount24h.compareTo(dailyAmountThreshold) >= 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(35));
                reasons.add("High daily transaction volume: " + totalAmount24h);
            }
        }

        // Fallback to repository check for velocity
        if (customerId != null && timestamp != null) {
            LocalDateTime velocityStart = timestamp.minusMinutes(velocityMinutes);
            long recentTxnCount = transactionRepository.countByCustomerIdAndTimestampBetween(
                    customerId,
                    velocityStart,
                    timestamp
            );

            if (recentTxnCount >= velocityCountThreshold) {
                riskScore = riskScore.add(BigDecimal.valueOf(40));
                reasons.add("High velocity: " + recentTxnCount + " transactions in " + velocityMinutes + " minutes");
            } else if (recentTxnCount >= velocityCountThreshold / 2) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("Moderate velocity: " + recentTxnCount + " transactions in " + velocityMinutes + " minutes");
            }
        }

        // Check structuring - amounts just below reporting threshold
        if (amount != null) {
            BigDecimal reportingThreshold = BigDecimal.valueOf(10000);
            BigDecimal structuringRange = BigDecimal.valueOf(500);
            if (amount.compareTo(reportingThreshold.subtract(structuringRange)) >= 0 &&
                amount.compareTo(reportingThreshold) < 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("Potential structuring: amount just below reporting threshold");
            }

            // Check round amounts (common in money laundering)
            if (amount.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) == 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(10));
                reasons.add("Round amount transaction: " + amount);
            }
        }

        // Check for high-risk AML merchants (including crypto)
        if (merchant != null) {
            String merchantUpper = merchant.toUpperCase();
            for (String riskMerchant : HIGH_RISK_AML_MERCHANTS) {
                if (merchantUpper.contains(riskMerchant)) {
                    riskScore = riskScore.add(BigDecimal.valueOf(25));
                    reasons.add("High-risk AML merchant: " + riskMerchant);
                    break;
                }
            }
        }

        // Check merchant category
        if (merchantCategory != null) {
            String categoryUpper = merchantCategory.toUpperCase();
            for (String riskMerchant : HIGH_RISK_AML_MERCHANTS) {
                if (categoryUpper.contains(riskMerchant)) {
                    riskScore = riskScore.add(BigDecimal.valueOf(25));
                    reasons.add("High-risk AML category: " + riskMerchant);
                    break;
                }
            }

            // Check for cash-intensive businesses
            for (String cashMerchant : CASH_INTENSIVE_MERCHANTS) {
                if (categoryUpper.contains(cashMerchant)) {
                    riskScore = riskScore.add(BigDecimal.valueOf(15));
                    reasons.add("Cash-intensive business: " + cashMerchant);
                    break;
                }
            }
        }

        // Check customer risk profile
        if (event.getCustomer() != null) {
            String riskLevel = event.getCustomer().getRiskLevel();
            if ("HIGH".equalsIgnoreCase(riskLevel)) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("High-risk customer profile");
            } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
                riskScore = riskScore.add(BigDecimal.valueOf(15));
                reasons.add("Medium-risk customer profile");
            }
        }

        // Check fraud signals for AML-related indicators
        if (event.getFraudSignals() != null) {
            if (Boolean.TRUE.equals(event.getFraudSignals().getBlacklistedMerchant())) {
                riskScore = riskScore.add(BigDecimal.valueOf(40));
                reasons.add("Blacklisted merchant - AML concern");
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
        } else if (riskScore.compareTo(BigDecimal.valueOf(35)) >= 0) {
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

        log.info("AMLAgent completed for txn {}: score={}, decision={}, reasons={}",
                event.getTxnId(), riskScore, decision, reasons.size());
        return result;
    }

    @Override
    public String getAgentName() {
        return "AMLAgent";
    }

    @Override
    public double getWeight() {
        return 0.25; // 25% weight in final decision
    }
}

// Made with Bob