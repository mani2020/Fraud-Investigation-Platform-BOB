package com.fraud.platform.model.nested;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested behavioral metrics used for internal fraud analysis payloads.
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorMetrics {

    @JsonProperty("transactionCount24h")
    private Integer transactionCount24h;

    @JsonProperty("totalAmount24h")
    private BigDecimal totalAmount24h;

    @JsonProperty("velocityScore")
    private BigDecimal velocityScore;

    @JsonProperty("unusualTime")
    private Boolean unusualTime;

    @JsonProperty("unusualAmount")
    private Boolean unusualAmount;

    @JsonProperty("unusualLocation")
    private Boolean unusualLocation;

    @JsonProperty("avgTransactionAmount")
    private BigDecimal avgTransactionAmount;

    @JsonProperty("maxTransactionAmount")
    private BigDecimal maxTransactionAmount;

}

// Made with Bob