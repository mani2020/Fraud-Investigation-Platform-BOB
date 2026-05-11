package com.fraud.platform.agents;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.AgentResult;

/**
 * Interface for fraud detection agents.
 * Each agent analyzes a specific aspect of the transaction.
 */
public interface FraudAgent {
    
    /**
     * Analyze transaction and return fraud assessment.
     *
     * @param transaction Transaction event to analyze
     * @return Agent analysis result with score and reasons
     */
    AgentResult analyze(TransactionEvent transaction);
    
    /**
     * Get agent name for identification.
     *
     * @return Agent name
     */
    String getAgentName();
    
    /**
     * Get agent weight in final decision (0.0 to 1.0).
     * Higher weight means more influence on final score.
     *
     * @return Agent weight
     */
    double getWeight();
}

// Made with Bob