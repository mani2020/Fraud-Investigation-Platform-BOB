package com.fraud.platform.agents;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.AgentResult;
import com.fraud.platform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Device fingerprinting fraud detection agent.
 * Analyzes device ID patterns, anomalies, and device history.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceAgent implements FraudAgent {

    private final TransactionRepository transactionRepository;

    private static final List<String> SUSPICIOUS_DEVICE_PATTERNS = List.of(
            "UNKNOWN", "EMULATOR", "ROOTED", "JAILBROKEN",
            "VPN", "PROXY", "TOR", "ANONYMOUS"
    );

    @Override
    public AgentResult analyze(TransactionEvent transaction) {
        long startTime = System.currentTimeMillis();
        log.debug("DeviceAgent analyzing transaction: {}", transaction.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        String deviceId = transaction.getDeviceId().toUpperCase();

        // Check for suspicious device patterns
        for (String pattern : SUSPICIOUS_DEVICE_PATTERNS) {
            if (deviceId.contains(pattern)) {
                riskScore = riskScore.add(BigDecimal.valueOf(35));
                reasons.add("Suspicious device pattern detected: " + pattern);
                break;
            }
        }

        // Check for generic/default device IDs
        if (deviceId.matches("^(DEVICE|DEFAULT|TEST|DEMO).*")) {
            riskScore = riskScore.add(BigDecimal.valueOf(25));
            reasons.add("Generic or default device ID: " + deviceId);
        }

        // Check device ID length (too short might be suspicious)
        if (deviceId.length() < 10) {
            riskScore = riskScore.add(BigDecimal.valueOf(15));
            reasons.add("Unusually short device ID");
        }

        // Check if device is new/unknown for this customer
        try {
            boolean isKnownDevice = transactionRepository.existsByCustomerIdAndDeviceIdBeforeTime(
                    transaction.getCustomerId(),
                    transaction.getDeviceId(),
                    transaction.getTimestamp()
            );

            if (!isKnownDevice) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("New/unknown device for customer: " + transaction.getDeviceId());
            }
        } catch (Exception e) {
            log.warn("Failed to check device history for customer {}: {}",
                    transaction.getCustomerId(), e.getMessage());
        }

        // Check for device sharing (multiple customers using same device)
        try {
            Long customerCount = transactionRepository.countDistinctCustomersByDeviceId(
                    transaction.getDeviceId()
            );

            if (customerCount != null && customerCount > 5) {
                riskScore = riskScore.add(BigDecimal.valueOf(20));
                reasons.add("Device shared by multiple customers: " + customerCount + " customers");
            } else if (customerCount != null && customerCount > 2) {
                riskScore = riskScore.add(BigDecimal.valueOf(10));
                reasons.add("Device used by " + customerCount + " customers");
            }
        } catch (Exception e) {
            log.warn("Failed to check device sharing for {}: {}",
                    transaction.getDeviceId(), e.getMessage());
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
                .confidence(BigDecimal.valueOf(0.75))
                .processingTimeMs(processingTime)
                .build();

        log.debug("DeviceAgent completed: score={}, decision={}", riskScore, decision);
        return result;
    }

    @Override
    public String getAgentName() {
        return "DeviceAgent";
    }

    @Override
    public double getWeight() {
        return 0.20; // 20% weight in final decision
    }
}

// Made with Bob