package com.fraud.platform.agents;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Geo-location fraud detection agent.
 * Analyzes transaction country, location patterns, VPN detection, and location mismatches.
 * Leverages nested location data and fraud signals for enhanced detection.
 */
@Component
@Slf4j
public class GeoAgent implements FraudAgent {

    // Trusted countries - low fraud risk, established banking systems
    private static final List<String> TRUSTED_COUNTRIES = List.of(
            "INDIA", "USA", "UNITED STATES", "UK", "UNITED KINGDOM",
            "CANADA", "AUSTRALIA", "GERMANY", "FRANCE", "JAPAN",
            "SINGAPORE", "SWITZERLAND", "NETHERLANDS", "SWEDEN",
            "NORWAY", "DENMARK", "FINLAND", "NEW ZEALAND", "AUSTRIA",
            "BELGIUM", "IRELAND", "LUXEMBOURG", "SPAIN", "ITALY"
    );

    private static final List<String> HIGH_RISK_COUNTRIES = List.of(
            "RUSSIA", "NIGERIA", "CHINA", "NORTH KOREA", "IRAN",
            "SYRIA", "VENEZUELA", "BELARUS", "MYANMAR"
    );

    private static final List<String> MEDIUM_RISK_COUNTRIES = List.of(
            "UKRAINE", "PAKISTAN", "BANGLADESH", "INDONESIA",
            "PHILIPPINES", "VIETNAM", "EGYPT"
    );

    @Override
    public AgentResult analyze(CanonicalFraudEvent event) {
        long startTime = System.currentTimeMillis();
        log.debug("GeoAgent analyzing event: {}", event.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        // Use helper method for safe access to country
        String country = event.getCountry();
        
        if (country != null) {
            final String countryUpper = country.toUpperCase();

            // Check if country is in trusted list
            boolean isTrustedCountry = TRUSTED_COUNTRIES.stream()
                    .anyMatch(trusted -> countryUpper.contains(trusted) || trusted.contains(countryUpper));

            // Check high-risk countries
            if (HIGH_RISK_COUNTRIES.contains(countryUpper)) {
                riskScore = riskScore.add(BigDecimal.valueOf(50));
                reasons.add("Transaction from high-risk country: " + countryUpper);
            }
            // Check medium-risk countries
            else if (MEDIUM_RISK_COUNTRIES.contains(countryUpper)) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("Transaction from medium-risk country: " + country);
            }
            // Check if country is NOT in trusted list (untrusted/unknown country)
            else if (!isTrustedCountry) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("Transaction from untrusted/unknown country: " + country);
            }
        }

        // Check nested location data for additional context
        if (event.getLocation() != null) {
            String city = event.getLocation().getCity();
            String region = event.getLocation().getRegion();
            
            if (city != null) {
                log.debug("Transaction from city: {}", city);
            }
            if (region != null) {
                log.debug("Transaction from region: {}", region);
            }
        }

        // Check fraud signals for VPN detection
        if (event.getFraudSignals() != null) {
            if (Boolean.TRUE.equals(event.getFraudSignals().getVpnDetected())) {
                riskScore = riskScore.add(BigDecimal.valueOf(30));
                reasons.add("VPN detected");
            }
            
            if (Boolean.TRUE.equals(event.getFraudSignals().getLocationMismatch())) {
                riskScore = riskScore.add(BigDecimal.valueOf(35));
                reasons.add("Location mismatch detected");
            }
        }

        // Check device data for VPN/proxy indicators
        if (event.getDevice() != null) {
            if (Boolean.TRUE.equals(event.getDevice().getVpnDetected())) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("VPN detected from device data");
            }
            
            if (Boolean.TRUE.equals(event.getDevice().getProxyDetected())) {
                riskScore = riskScore.add(BigDecimal.valueOf(25));
                reasons.add("Proxy detected");
            }
            
            // Check IP address for additional risk scoring
            String ipAddress = event.getDevice().getIpAddress();
            if (ipAddress != null) {
                // Check for suspicious IP patterns (e.g., Tor exit nodes, known VPN ranges)
                if (ipAddress.startsWith("10.") || ipAddress.startsWith("192.168.")) {
                    riskScore = riskScore.add(BigDecimal.valueOf(15));
                    reasons.add("Private IP address detected: " + ipAddress);
                }
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
                .confidence(BigDecimal.valueOf(0.80))
                .processingTimeMs(processingTime)
                .build();

        log.info("GeoAgent completed for txn {}: score={}, decision={}, reasons={}",
                event.getTxnId(), riskScore, decision, reasons.size());
        return result;
    }

    @Override
    public String getAgentName() {
        return "GeoAgent";
    }

    @Override
    public double getWeight() {
        return 0.20; // 20% weight in final decision
    }
}

// Made with Bob