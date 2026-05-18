package com.fraud.platform.model.nested;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested merchant details used for internal fraud analysis payloads.
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class MerchantInfo {

    /**
     * Unique merchant identifier.
     */
    private String merchantId;

    /**
     * Merchant display name.
     */
    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    /**
     * Merchant category classification.
     */
    private String merchantCategory;

    /**
     * Merchant country code or country name.
     */
    private String country;

    /**
     * Historical merchant fraud rate between 0.0 and 1.0.
     */
    @DecimalMin(value = "0.0", message = "Fraud rate must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Fraud rate must be at most 1.0")
    private BigDecimal fraudRate;

    /**
     * Merchant risk level classification.
     */
    private String riskLevel;

    /**
     * Indicates whether the merchant is blacklisted.
     */
    private Boolean isBlacklisted;
}

// Made with Bob