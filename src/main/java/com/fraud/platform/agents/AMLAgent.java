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
 * Anti-Money Laundering (AML) fraud detection agent.
 * Analyzes transaction patterns for money laundering indicators.
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

    // High-risk merchants for money laundering
    private static final List<String> HIGH_RISK_AML_MERCHANTS = List.of(
            "CRYPTO", "EXCHANGE", "CASINO", "GAMBLING", "BETTING",
            "MONEY_TRANSFER", "REMITTANCE", "FOREX", "GOLD", "JEWELRY",
            "PAWN", "AUCTION", "DEALER", "BROKER"
    );

    // Cash-intensive businesses (higher AML risk)
    private static final List<String> CASH_INTENSIVE_MERCHANTS = List.of(
            "ATM", "CASH", "CHECK_CASHING", "PAYDAY", "LOAN",
            "BAR", "NIGHTCLUB", "LIQUOR", "TOBACCO", "VAPE"
    );

    @Override
    public AgentResult analyze(TransactionEvent transaction) {
        long startTime = System.currentTimeMillis();
        log.debug("AMLAgent analyzing transaction: {}", transaction.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        String merchant = transaction.getMerchant().toUpperCase();

        // Check velocity - multiple transactions in short time
        LocalDateTime velocityStart = transaction.getTimestamp().minusMinutes(velocityMinutes);
        long recentTxnCount = transactionRepository.countByCustomerIdAndTimestampBetween(
                transaction.getCustomerId(),
                velocityStart,
                transaction.getTimestamp()
        );

        if (recentTxnCount >= velocityCountThreshold) {
            riskScore = riskScore.add(BigDecimal.valueOf(40));
            reasons.add("High velocity: " + recentTxnCount + " transactions in " + velocityMinutes + " minutes");
        } else if (recentTxnCount >= velocityCountThreshold / 2) {
            riskScore = riskScore.add(BigDecimal.valueOf(20));
            reasons.add("Moderate velocity: " + recentTxnCount + " transactions in " + velocityMinutes + " minutes");
        }

        // Check structuring - amounts just below reporting threshold
        BigDecimal reportingThreshold = BigDecimal.valueOf(10000);
        BigDecimal structuringRange = BigDecimal.valueOf(500);
        if (transaction.getAmount().compareTo(reportingThreshold.subtract(structuringRange)) >= 0 &&
            transaction.getAmount().compareTo(reportingThreshold) < 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(30));
            reasons.add("Potential structuring: amount just below reporting threshold");
        }

        // Check round amounts (common in money laundering)
        if (transaction.getAmount().remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) == 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(10));
            reasons.add("Round amount transaction: " + transaction.getAmount());
        }

        // Check for high-risk AML merchants
        for (String riskMerchant : HIGH_RISK_AML_MERCHANTS) {
            if (merchant.contains(riskMerchant)) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("High-risk AML merchant: " + riskMerchant);
                break;
            }
        }

        // Check for cash-intensive businesses
        for (String cashMerchant : CASH_INTENSIVE_MERCHANTS) {
            if (merchant.contains(cashMerchant)) {
                riskScore = riskScore.add(BigDecimal.valueOf(15));
                reasons.add("Cash-intensive business: " + cashMerchant);
                break;
            }
        }

        // TODO: Add layering detection (rapid movement between accounts)
        // TODO: Add integration detection (funds entering legitimate economy)
        // TODO: Add smurfing detection (multiple small transactions)

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

        log.debug("AMLAgent completed: score={}, decision={}", riskScore, decision);
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