package com.fraud.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Result from a fraud detection agent analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {
    
    /**
     * Agent name that produced this result.
     */
    private String agentName;
    
    /**
     * Fraud risk score (0.0 to 100.0).
     * 0 = No risk, 100 = Definite fraud
     */
    private BigDecimal riskScore;
    
    /**
     * Agent's decision: APPROVE, REVIEW, REJECT.
     */
    private String decision;
    
    /**
     * Reasons for the decision (explainable AI).
     */
    @Builder.Default
    private List<String> reasons = new ArrayList<>();
    
    /**
     * Confidence level (0.0 to 1.0).
     */
    private BigDecimal confidence;
    
    /**
     * Processing time in milliseconds.
     */
    private Long processingTimeMs;
    
    /**
     * Add a reason to the list.
     */
    public void addReason(String reason) {
        if (reasons == null) {
            reasons = new ArrayList<>();
        }
        reasons.add(reason);
    }
}

// Made with Bob