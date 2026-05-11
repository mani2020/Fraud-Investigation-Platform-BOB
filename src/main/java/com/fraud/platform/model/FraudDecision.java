package com.fraud.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Final fraud decision aggregated from all agents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDecision {
    
    /**
     * Transaction ID.
     */
    private String txnId;
    
    /**
     * Final fraud risk score (0.0 to 100.0).
     * Weighted average of all agent scores.
     */
    private BigDecimal finalScore;
    
    /**
     * Final decision: APPROVE, REVIEW, REJECT.
     */
    private String decision;
    
    /**
     * All reasons from all agents (explainable AI).
     */
    @Builder.Default
    private List<String> allReasons = new ArrayList<>();
    
    /**
     * Individual agent results.
     */
    @Builder.Default
    private List<AgentResult> agentResults = new ArrayList<>();
    
    /**
     * Decision timestamp.
     */
    private LocalDateTime timestamp;
    
    /**
     * Total processing time in milliseconds.
     */
    private Long totalProcessingTimeMs;
    
    /**
     * Confidence level (0.0 to 1.0).
     */
    private BigDecimal confidence;
    
    /**
     * Risk factors detected.
     */
    private Map<String, Object> riskFactors;
    
    /**
     * Human-readable explanation for fraud investigators.
     */
    private String humanReadableExplanation;
    
    /**
     * Short summary for dashboard/alerts.
     */
    private String shortSummary;
    
    /**
     * Add an agent result.
     */
    public void addAgentResult(AgentResult result) {
        if (agentResults == null) {
            agentResults = new ArrayList<>();
        }
        agentResults.add(result);
        
        // Add agent reasons to all reasons
        if (result.getReasons() != null) {
            if (allReasons == null) {
                allReasons = new ArrayList<>();
            }
            result.getReasons().forEach(reason -> 
                allReasons.add(result.getAgentName() + ": " + reason)
            );
        }
    }
}

// Made with Bob