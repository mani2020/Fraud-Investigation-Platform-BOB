package com.fraud.platform.agents;

import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;

/**
 * Interface for fraud detection agents.
 * Each agent analyzes a specific aspect of the fraud event.
 */
public interface FraudAgent {
    
    /**
     * Analyze fraud event and return fraud assessment.
     * Agents should leverage nested data structures (transaction, merchant, device, location, etc.)
     * and fraud signals for enhanced detection capabilities.
     *
     * @param event Canonical fraud event with nested data to analyze
     * @return Agent analysis result with score and reasons
     */
    AgentResult analyze(CanonicalFraudEvent event);
    
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