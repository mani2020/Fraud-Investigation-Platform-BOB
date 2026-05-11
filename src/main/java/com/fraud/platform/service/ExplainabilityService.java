package com.fraud.platform.service;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.FraudDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for converting technical fraud analysis into human-readable explanations.
 * Provides banking fraud investigators with clear, actionable insights.
 */
@Service
@Slf4j
public class ExplainabilityService {

    /**
     * Generate human-readable explanation for fraud decision.
     *
     * @param decision Fraud decision with agent results
     * @return Human-readable explanation
     */
    public String generateExplanation(FraudDecision decision) {
        log.debug("Generating explanation for transaction: {}", decision.getTxnId());

        StringBuilder explanation = new StringBuilder();
        
        // Header with decision
        explanation.append(generateDecisionHeader(decision));
        explanation.append("\n\n");
        
        // Risk summary
        explanation.append(generateRiskSummary(decision));
        explanation.append("\n\n");
        
        // Detailed findings by agent
        explanation.append(generateDetailedFindings(decision));
        
        // Recommendations
        explanation.append("\n\n");
        explanation.append(generateRecommendations(decision));
        
        return explanation.toString();
    }

    /**
     * Generate decision header with overall verdict.
     */
    private String generateDecisionHeader(FraudDecision decision) {
        String emoji = switch (decision.getDecision()) {
            case "REJECT" -> "🚫";
            case "REVIEW" -> "⚠️";
            case "APPROVE" -> "✅";
            default -> "❓";
        };
        
        return String.format("%s TRANSACTION %s\n" +
                           "Transaction ID: %s\n" +
                           "Risk Score: %.2f/100\n" +
                           "Confidence: %.0f%%",
                emoji,
                decision.getDecision(),
                decision.getTxnId(),
                decision.getFinalScore(),
                decision.getConfidence().multiply(BigDecimal.valueOf(100)));
    }

    /**
     * Generate risk summary section.
     */
    private String generateRiskSummary(FraudDecision decision) {
        StringBuilder summary = new StringBuilder("RISK SUMMARY:\n");
        
        List<String> primaryConcerns = extractPrimaryConcerns(decision);
        
        if (primaryConcerns.isEmpty()) {
            summary.append("• No significant fraud indicators detected");
        } else {
            summary.append("Transaction ").append(decision.getDecision().toLowerCase())
                   .append(" because:\n");
            
            for (String concern : primaryConcerns) {
                summary.append("• ").append(concern).append("\n");
            }
        }
        
        return summary.toString().trim();
    }

    /**
     * Extract primary concerns from agent results.
     */
    private List<String> extractPrimaryConcerns(FraudDecision decision) {
        List<String> concerns = new ArrayList<>();
        
        for (AgentResult result : decision.getAgentResults()) {
            if (result.getRiskScore().compareTo(BigDecimal.valueOf(20)) > 0) {
                concerns.addAll(translateReasons(result.getAgentName(), result.getReasons()));
            }
        }
        
        return concerns.stream().distinct().limit(5).collect(Collectors.toList());
    }

    /**
     * Translate technical reasons to human-readable format.
     */
    private List<String> translateReasons(String agentName, List<String> reasons) {
        return reasons.stream()
                .map(reason -> translateReason(agentName, reason))
                .collect(Collectors.toList());
    }

    /**
     * Translate individual reason to human-readable format.
     */
    private String translateReason(String agentName, String reason) {
        String lowerReason = reason.toLowerCase();
        
        // Amount-related
        if (lowerReason.contains("high-value") || lowerReason.contains("suspicious transaction amount")) {
            return "Amount exceeds normal spending pattern";
        }
        if (lowerReason.contains("abnormal amount") || lowerReason.contains("10x customer average")) {
            return "Transaction amount significantly higher than customer's typical spending";
        }
        if (lowerReason.contains("elevated amount") || lowerReason.contains("5x customer average")) {
            return "Transaction amount moderately higher than usual";
        }
        
        // Country/Geography
        if (lowerReason.contains("high-risk country")) {
            return "Payment originated from high-risk country";
        }
        if (lowerReason.contains("medium-risk country")) {
            return "Payment originated from medium-risk country";
        }
        if (lowerReason.contains("untrusted") && lowerReason.contains("country")) {
            return "Payment originated from untrusted/unknown country";
        }
        
        // Device
        if (lowerReason.contains("suspicious device pattern")) {
            return "Suspicious device characteristics detected (emulator, VPN, or rooted device)";
        }
        if (lowerReason.contains("new/unknown device")) {
            return "Transaction from previously unknown device";
        }
        if (lowerReason.contains("generic or default device")) {
            return "Generic or suspicious device identifier detected";
        }
        if (lowerReason.contains("device shared by multiple customers")) {
            return "Device associated with multiple customer accounts";
        }
        
        // Merchant
        if (lowerReason.contains("high-risk merchant")) {
            return "Transaction with high-risk merchant category (crypto, gambling, forex)";
        }
        if (lowerReason.contains("high-risk aml merchant")) {
            return "Merchant category associated with money laundering risk";
        }
        if (lowerReason.contains("cash-intensive business")) {
            return "Transaction with cash-intensive business type";
        }
        
        // Payment Type
        if (lowerReason.contains("high-risk payment type")) {
            return "High-risk payment method used (crypto, wire transfer, international)";
        }
        
        // Velocity/Frequency
        if (lowerReason.contains("high velocity")) {
            return "Unusually high transaction frequency detected";
        }
        if (lowerReason.contains("rapid transaction burst") || lowerReason.contains("rapid burst")) {
            return "Multiple transactions in very short time period";
        }
        if (lowerReason.contains("elevated transaction frequency")) {
            return "Higher than normal transaction frequency";
        }
        
        // Timing
        if (lowerReason.contains("high-risk hours") || lowerReason.contains("2 am - 5 am")) {
            return "Transaction during high-risk hours (2 AM - 5 AM)";
        }
        if (lowerReason.contains("unusual hours")) {
            return "Transaction during unusual hours (late night/early morning)";
        }
        if (lowerReason.contains("weekend transaction")) {
            return "Transaction occurred during weekend";
        }
        if (lowerReason.contains("public holiday")) {
            return "Transaction occurred on public holiday";
        }
        
        // AML Patterns
        if (lowerReason.contains("structuring")) {
            return "Potential structuring detected (amount just below reporting threshold)";
        }
        if (lowerReason.contains("round amount")) {
            return "Round amount transaction (common in money laundering)";
        }
        
        // Customer History
        if (lowerReason.contains("first transaction")) {
            return "First transaction for this customer (no historical pattern)";
        }
        
        // Default: return original reason
        return reason;
    }

    /**
     * Generate detailed findings by agent.
     */
    private String generateDetailedFindings(FraudDecision decision) {
        StringBuilder findings = new StringBuilder("DETAILED ANALYSIS:\n\n");
        
        for (AgentResult result : decision.getAgentResults()) {
            findings.append(generateAgentFindings(result));
            findings.append("\n");
        }
        
        return findings.toString().trim();
    }

    /**
     * Generate findings for individual agent.
     */
    private String generateAgentFindings(AgentResult result) {
        StringBuilder agentFindings = new StringBuilder();
        
        String agentDisplayName = getAgentDisplayName(result.getAgentName());
        String riskLevel = getRiskLevel(result.getRiskScore());
        
        agentFindings.append(String.format("📊 %s Analysis: %s Risk (Score: %.0f)\n",
                agentDisplayName, riskLevel, result.getRiskScore()));
        
        if (!result.getReasons().isEmpty()) {
            for (String reason : result.getReasons()) {
                agentFindings.append("   • ").append(translateReason(result.getAgentName(), reason)).append("\n");
            }
        } else {
            agentFindings.append("   • No concerns identified\n");
        }
        
        return agentFindings.toString();
    }

    /**
     * Get human-readable agent name.
     */
    private String getAgentDisplayName(String agentName) {
        return switch (agentName) {
            case "RiskAgent" -> "Transaction Risk";
            case "GeoAgent" -> "Geographic Location";
            case "DeviceAgent" -> "Device Fingerprint";
            case "AMLAgent" -> "Anti-Money Laundering";
            case "BehaviorAgent" -> "Behavioral Pattern";
            default -> agentName;
        };
    }

    /**
     * Get risk level description.
     */
    private String getRiskLevel(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "CRITICAL";
        } else if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "HIGH";
        } else if (score.compareTo(BigDecimal.valueOf(30)) >= 0) {
            return "MEDIUM";
        } else if (score.compareTo(BigDecimal.valueOf(10)) >= 0) {
            return "LOW";
        } else {
            return "MINIMAL";
        }
    }

    /**
     * Generate recommendations based on decision.
     */
    private String generateRecommendations(FraudDecision decision) {
        StringBuilder recommendations = new StringBuilder("RECOMMENDED ACTIONS:\n");
        
        switch (decision.getDecision()) {
            case "REJECT" -> {
                recommendations.append("• BLOCK transaction immediately\n");
                recommendations.append("• Contact customer to verify transaction legitimacy\n");
                recommendations.append("• Flag account for enhanced monitoring\n");
                recommendations.append("• Consider temporary account restrictions\n");
                recommendations.append("• Document all findings for compliance review");
            }
            case "REVIEW" -> {
                recommendations.append("• HOLD transaction for manual review\n");
                recommendations.append("• Contact customer for additional verification\n");
                recommendations.append("• Request supporting documentation if needed\n");
                recommendations.append("• Review customer's recent transaction history\n");
                recommendations.append("• Escalate to senior fraud analyst if concerns persist");
            }
            case "APPROVE" -> {
                recommendations.append("• APPROVE transaction for processing\n");
                recommendations.append("• Continue standard monitoring\n");
                recommendations.append("• No immediate action required");
            }
            default -> recommendations.append("• Review transaction details and make manual decision");
        }
        
        return recommendations.toString();
    }

    /**
     * Generate short summary for dashboard/alerts.
     */
    public String generateShortSummary(FraudDecision decision) {
        List<String> concerns = extractPrimaryConcerns(decision);
        
        if (concerns.isEmpty()) {
            return String.format("Transaction %s - No significant fraud indicators", 
                    decision.getDecision());
        }
        
        String topConcern = concerns.get(0);
        int additionalCount = concerns.size() - 1;
        
        if (additionalCount > 0) {
            return String.format("Transaction %s - %s (+%d more concerns)", 
                    decision.getDecision(), topConcern, additionalCount);
        } else {
            return String.format("Transaction %s - %s", 
                    decision.getDecision(), topConcern);
        }
    }
}

// Made with Bob