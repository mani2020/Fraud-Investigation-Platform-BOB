package com.fraud.platform.service;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.FraudDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for making final fraud decisions based on aggregated agent scores.
 * Determines one of four outcomes: APPROVE, OTP, HOLD, or BLOCK.
 */
@Service
@Slf4j
public class DecisionService {

    @Value("${fraud.decision.approve-threshold:30}")
    private int approveThreshold;

    @Value("${fraud.decision.otp-threshold:50}")
    private int otpThreshold;

    @Value("${fraud.decision.hold-threshold:70}")
    private int holdThreshold;

    @Value("${fraud.decision.block-threshold:85}")
    private int blockThreshold;

    /**
     * Make final fraud decision based on agent scores.
     *
     * @param decision Fraud decision with agent results
     * @return Updated decision with final verdict
     */
    public String makeDecision(FraudDecision decision) {
        BigDecimal totalScore = decision.getFinalScore();
        int score = totalScore.intValue();

        log.debug("Making decision for transaction {} with score: {}", 
                decision.getTxnId(), score);

        String finalDecision;
        if (score >= blockThreshold) {
            finalDecision = "BLOCK";
        } else if (score >= holdThreshold) {
            finalDecision = "HOLD";
        } else if (score >= otpThreshold) {
            finalDecision = "OTP";
        } else if (score >= approveThreshold) {
            finalDecision = "OTP"; // Low-medium risk still requires OTP
        } else {
            finalDecision = "APPROVE";
        }

        log.info("Decision for {}: {} (Score: {})", 
                decision.getTxnId(), finalDecision, score);

        return finalDecision;
    }

    /**
     * Generate decision matrix showing agent scores and final decision.
     *
     * @param decision Fraud decision with agent results
     * @return Formatted decision matrix
     */
    public String generateDecisionMatrix(FraudDecision decision) {
        StringBuilder matrix = new StringBuilder();
        
        matrix.append("╔════════════════════════════════════════╗\n");
        matrix.append("║     FRAUD DECISION MATRIX              ║\n");
        matrix.append("╠════════════════════════════════════════╣\n");
        matrix.append(String.format("║ Transaction ID: %-22s ║\n", decision.getTxnId()));
        matrix.append("╠════════════════════════════════════════╣\n");
        matrix.append("║ Agent Scores:                          ║\n");
        matrix.append("╠════════════════════════════════════════╣\n");

        // Agent scores table
        for (AgentResult result : decision.getAgentResults()) {
            String agentName = formatAgentName(result.getAgentName());
            int score = result.getRiskScore().intValue();
            String bar = generateScoreBar(score);
            
            matrix.append(String.format("║ %-20s %3d %s ║\n", 
                    agentName, score, bar));
        }

        matrix.append("╠════════════════════════════════════════╣\n");
        
        // Total score
        int totalScore = decision.getFinalScore().intValue();
        String totalBar = generateScoreBar(totalScore);
        matrix.append(String.format("║ %-20s %3d %s ║\n", 
                "TOTAL SCORE", totalScore, totalBar));
        
        matrix.append("╠════════════════════════════════════════╣\n");
        
        // Decision thresholds
        matrix.append("║ Decision Thresholds:                   ║\n");
        matrix.append(String.format("║   APPROVE:  < %-3d                       ║\n", approveThreshold));
        matrix.append(String.format("║   OTP:      %-3d - %-3d                   ║\n", 
                approveThreshold, otpThreshold - 1));
        matrix.append(String.format("║   HOLD:     %-3d - %-3d                   ║\n", 
                otpThreshold, holdThreshold - 1));
        matrix.append(String.format("║   BLOCK:    ≥ %-3d                       ║\n", blockThreshold));
        
        matrix.append("╠════════════════════════════════════════╣\n");
        
        // Final decision
        String finalDecision = decision.getDecision();
        String decisionEmoji = getDecisionEmoji(finalDecision);
        matrix.append(String.format("║ FINAL DECISION: %s %-18s ║\n", 
                decisionEmoji, finalDecision));
        
        matrix.append("╚════════════════════════════════════════╝\n");

        return matrix.toString();
    }

    /**
     * Generate simple decision table (for logs/APIs).
     */
    public String generateSimpleTable(FraudDecision decision) {
        StringBuilder table = new StringBuilder();
        
        table.append("\n");
        table.append("┌──────────────────────┬───────┐\n");
        table.append("│ Agent                │ Score │\n");
        table.append("├──────────────────────┼───────┤\n");

        for (AgentResult result : decision.getAgentResults()) {
            String agentName = formatAgentName(result.getAgentName());
            int score = result.getRiskScore().intValue();
            table.append(String.format("│ %-20s │ %5d │\n", agentName, score));
        }

        table.append("├──────────────────────┼───────┤\n");
        int totalScore = decision.getFinalScore().intValue();
        table.append(String.format("│ %-20s │ %5d │\n", "TOTAL", totalScore));
        table.append("└──────────────────────┴───────┘\n");
        table.append("\n");
        table.append(String.format("Decision: %s\n", decision.getDecision()));

        return table.toString();
    }

    /**
     * Generate Markdown table for documentation/reports.
     */
    public String generateMarkdownTable(FraudDecision decision) {
        StringBuilder md = new StringBuilder();
        
        md.append("## Fraud Decision Matrix\n\n");
        md.append(String.format("**Transaction ID:** %s  \n", decision.getTxnId()));
        md.append(String.format("**Timestamp:** %s  \n\n", decision.getTimestamp()));
        
        md.append("| Agent | Score | Weight | Weighted Score |\n");
        md.append("|-------|-------|--------|----------------|\n");

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (AgentResult result : decision.getAgentResults()) {
            String agentName = formatAgentName(result.getAgentName());
            BigDecimal score = result.getRiskScore();
            double weight = getAgentWeight(result.getAgentName());
            BigDecimal weightedScore = score.multiply(BigDecimal.valueOf(weight));
            
            totalWeightedScore = totalWeightedScore.add(weightedScore);
            totalWeight = totalWeight.add(BigDecimal.valueOf(weight));

            md.append(String.format("| %s | %.0f | %.0f%% | %.2f |\n", 
                    agentName, score, weight * 100, weightedScore));
        }

        md.append("|-------|-------|--------|----------------|\n");
        BigDecimal finalScore = totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        md.append(String.format("| **TOTAL** | **%.0f** | **100%%** | **%.2f** |\n\n", 
                finalScore, finalScore));

        md.append("### Decision Thresholds\n\n");
        md.append(String.format("- **APPROVE:** < %d\n", approveThreshold));
        md.append(String.format("- **OTP:** %d - %d\n", approveThreshold, otpThreshold - 1));
        md.append(String.format("- **HOLD:** %d - %d\n", otpThreshold, holdThreshold - 1));
        md.append(String.format("- **BLOCK:** ≥ %d\n\n", blockThreshold));

        md.append(String.format("### Final Decision: **%s** %s\n\n", 
                decision.getDecision(), getDecisionEmoji(decision.getDecision())));

        return md.toString();
    }

    /**
     * Get decision explanation based on score.
     */
    public String getDecisionExplanation(String decision) {
        return switch (decision) {
            case "APPROVE" -> "Transaction approved - Low fraud risk detected. Process normally.";
            case "OTP" -> "OTP verification required - Medium fraud risk detected. Additional authentication needed.";
            case "HOLD" -> "Transaction on hold - High fraud risk detected. Manual review required before processing.";
            case "BLOCK" -> "Transaction blocked - Critical fraud risk detected. Do not process. Contact customer immediately.";
            default -> "Unknown decision";
        };
    }

    /**
     * Get decision action items.
     */
    public List<String> getDecisionActions(String decision) {
        List<String> actions = new ArrayList<>();
        
        switch (decision) {
            case "APPROVE" -> {
                actions.add("Process transaction normally");
                actions.add("Continue standard monitoring");
                actions.add("No additional verification required");
            }
            case "OTP" -> {
                actions.add("Send OTP to registered mobile number");
                actions.add("Wait for customer verification");
                actions.add("Process only after successful OTP validation");
                actions.add("Log OTP attempt for audit trail");
            }
            case "HOLD" -> {
                actions.add("Place transaction on hold");
                actions.add("Assign to fraud analyst for manual review");
                actions.add("Contact customer for verification");
                actions.add("Request supporting documentation");
                actions.add("Review within 24 hours");
            }
            case "BLOCK" -> {
                actions.add("Block transaction immediately");
                actions.add("Send alert to customer via SMS/email");
                actions.add("Flag account for enhanced monitoring");
                actions.add("Initiate fraud investigation");
                actions.add("Consider temporary account freeze");
                actions.add("Document all findings for compliance");
            }
        }
        
        return actions;
    }

    /**
     * Format agent name for display.
     */
    private String formatAgentName(String agentName) {
        return agentName.replace("Agent", "");
    }

    /**
     * Generate visual score bar.
     */
    private String generateScoreBar(int score) {
        int bars = score / 10;
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                if (score >= 85) {
                    bar.append("█"); // Critical
                } else if (score >= 70) {
                    bar.append("▓"); // High
                } else if (score >= 50) {
                    bar.append("▒"); // Medium
                } else {
                    bar.append("░"); // Low
                }
            } else {
                bar.append("·");
            }
        }
        
        return bar.toString();
    }

    /**
     * Get emoji for decision.
     */
    private String getDecisionEmoji(String decision) {
        return switch (decision) {
            case "APPROVE" -> "✅";
            case "OTP" -> "🔐";
            case "HOLD" -> "⏸️";
            case "BLOCK" -> "🚫";
            default -> "❓";
        };
    }

    /**
     * Get agent weight.
     */
    private double getAgentWeight(String agentName) {
        return switch (agentName) {
            case "RiskAgent" -> 0.25;
            case "GeoAgent" -> 0.20;
            case "DeviceAgent" -> 0.20;
            case "AMLAgent" -> 0.25;
            case "BehaviorAgent" -> 0.10;
            default -> 0.20;
        };
    }
}

// Made with Bob