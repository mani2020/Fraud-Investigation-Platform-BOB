package com.fraud.platform.model.nested;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Nested behavioral metrics used for internal fraud analysis payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorMetrics {

    /**
     * Number of transactions observed in the last 24 hours.
     */
    private Integer transactionCount24h;

    /**
     * Total transaction amount observed in the last 24 hours.
     */
    private BigDecimal totalAmount24h;

    /**
     * Calculated velocity score for recent transaction activity.
     */
    private BigDecimal velocityScore;

    /**
     * Indicates whether the transaction occurred at an unusual time.
     */
    private Boolean unusualTime;

    /**
     * Indicates whether the amount is unusual for the customer.
     */
    private Boolean unusualAmount;

    /**
     * Indicates whether the location is unusual for the customer.
     */
    private Boolean unusualLocation;

    /**
     * Average customer transaction amount.
     */
    private BigDecimal avgTransactionAmount;

    /**
     * Maximum customer transaction amount observed.
     */
    private BigDecimal maxTransactionAmount;
}

// Made with Bob