package com.fraud.platform.agents;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Device fingerprinting fraud detection agent.
 * Analyzes device ID, trust status, fingerprints, VPN/proxy detection, and
 * device history.
 * Leverages nested device data and fraud signals for enhanced detection.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceAgent implements FraudAgent {

    private final TransactionRepository transactionRepository;

    private static final List<String> SUSPICIOUS_DEVICE_PATTERNS = List.of(
            "UNKNOWN", "EMULATOR", "ROOTED", "JAILBROKEN",
            "VPN", "PROXY", "TOR", "ANONYMOUS");

    @Override
    public AgentResult analyze(CanonicalFraudEvent event) {
        long startTime = System.currentTimeMillis();
        log.debug("DeviceAgent analyzing event: {}", event.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Use helper method for safe access to device ID
        String deviceId = event.getDeviceId();
        String customerId = event.getCustomerId();

        if (deviceId != null) {
            String deviceIdUpper = deviceId.toUpperCase();

            // Check for suspicious device patterns
            for (String pattern : SUSPICIOUS_DEVICE_PATTERNS) {
                if (deviceIdUpper.contains(pattern)) {
                    riskScore = riskScore.add(BigDecimal.valueOf(35));
                    reasons.add("Suspicious device pattern detected: " + pattern);
                    break;
                }
            }

            // Check for generic/default device IDs
            if (deviceIdUpper.matches("^(DEVICE|DEFAULT|TEST|DEMO).*")) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("Generic or default device ID: " + deviceId);
            }

            // Check device ID length (too short might be suspicious)
            if (deviceId.length() < 10) {
                riskScore = riskScore.add(BigDecimal.valueOf(15));
                reasons.add("Unusually short device ID");
            }

            // Check if device is new/unknown for this customer
            if (customerId != null && event.getTimestamp() != null) {
                try {
                    boolean isKnownDevice = transactionRepository.existsByCustomerIdAndDeviceIdBeforeTime(
                            customerId,
                            deviceId,
                            event.getTimestamp());

                    if (!isKnownDevice) {
                        riskScore = riskScore.add(BigDecimal.valueOf(30));
                        reasons.add("New/unknown device for customer: " + deviceId);
                    }
                } catch (Exception e) {
                    log.warn("Failed to check device history for customer {}: {}",
                            customerId, e.getMessage());
                }
            }

            // Check for device sharing (multiple customers using same device)
            try {
                Long customerCount = transactionRepository.countDistinctCustomersByDeviceId(deviceId);

                if (customerCount != null && customerCount > 5) {
                    riskScore = riskScore.add(BigDecimal.valueOf(20));
                    reasons.add("Device shared by multiple customers: " + customerCount + " customers");
                } else if (customerCount != null && customerCount > 2) {
                    riskScore = riskScore.add(BigDecimal.valueOf(10));
                    reasons.add("Device used by " + customerCount + " customers");
                }
            } catch (Exception e) {
                log.warn("Failed to check device sharing for {}: {}", deviceId, e.getMessage());
            }
        }

        // Check nested device data for enhanced detection
        if (event.getDevice() != null) {
            String userAgent = event.getDevice().getUserAgent();
            // Check for TOR browser
            if (userAgent != null
                    && userAgent.toLowerCase().contains("tor")) {

                riskScore = riskScore.add(BigDecimal.valueOf(50));

                reasons.add("TOR browser detected");
            }

            // Check device trust status
            if (Boolean.FALSE.equals(event.getDevice().getIsTrusted())) {
                riskScore = riskScore.add(BigDecimal.valueOf(40));
                reasons.add("Device is not trusted");
            }

            // Check device fingerprint
            String fingerprint = event.getDevice().getDeviceFingerprint();
            if (fingerprint != null) {
                // Check for suspicious fingerprint patterns
                if (fingerprint.length() < 20) {
                    riskScore = riskScore.add(BigDecimal.valueOf(20));
                    reasons.add("Suspicious device fingerprint: too short");
                }
                log.debug("Device fingerprint analyzed: {}", fingerprint);
            }

            // Check VPN detection
            if (Boolean.TRUE.equals(event.getDevice().getVpnDetected())) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("VPN detected on device");
            }

            // Check proxy detection
            if (Boolean.TRUE.equals(event.getDevice().getProxyDetected())) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("Proxy detected on device");
            }
        }

        // Check fraud signals for device-related issues
        if (event.getFraudSignals() != null) {
            if (Boolean.TRUE.equals(event.getFraudSignals().getDeviceMismatch())) {
                riskScore = riskScore.add(BigDecimal.valueOf(45));
                reasons.add("Device mismatch detected");
            }

            if (Boolean.TRUE.equals(event.getFraudSignals().getBlacklistedDevice())) {
                riskScore = riskScore.add(BigDecimal.valueOf(60));
                reasons.add("Device is blacklisted");
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
                .confidence(BigDecimal.valueOf(0.75))
                .processingTimeMs(processingTime)
                .build();

        log.info("DeviceAgent completed for txn {}: score={}, decision={}, reasons={}",
                event.getTxnId(), riskScore, decision, reasons.size());
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