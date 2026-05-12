package com.fraud.platform.agents;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.AgentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Geo-location fraud detection agent.
 * Analyzes transaction country and location patterns with trusted country whitelist.
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
    public AgentResult analyze(TransactionEvent transaction) {
        long startTime = System.currentTimeMillis();
        log.debug("GeoAgent analyzing transaction: {}", transaction.getTxnId());

        BigDecimal riskScore = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        String country = transaction.getCountry().toUpperCase();

        // Check if country is in trusted list
        boolean isTrustedCountry = TRUSTED_COUNTRIES.stream()
                .anyMatch(trusted -> country.contains(trusted) || trusted.contains(country));

        // Check high-risk countries
        if (HIGH_RISK_COUNTRIES.contains(country)) {
            riskScore = riskScore.add(BigDecimal.valueOf(50));
            reasons.add("Transaction from high-risk country: " + country);
        }
        // Check medium-risk countries
        else if (MEDIUM_RISK_COUNTRIES.contains(country)) {
            riskScore = riskScore.add(BigDecimal.valueOf(25));
            reasons.add("Transaction from medium-risk country: " + country);
        }
        // Check if country is NOT in trusted list (untrusted/unknown country)
        else if (!isTrustedCountry) {
            riskScore = riskScore.add(BigDecimal.valueOf(30));
            reasons.add("Transaction from untrusted/unknown country: " + country);
        }

        // TODO: Add velocity checks - multiple countries in short time
        // TODO: Add impossible travel detection
        // TODO: Add IP geolocation mismatch detection

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

        log.debug("GeoAgent completed: score={}, decision={}", riskScore, decision);
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