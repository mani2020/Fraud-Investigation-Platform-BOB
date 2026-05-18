package com.fraud.platform.model.nested;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested fraud signal indicators used for internal fraud analysis payloads.
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class FraudSignals {

    /**
     * Indicates whether VPN usage was detected.
     */
    private Boolean vpnDetected;

    /**
     * Indicates whether proxy usage was detected.
     */
    private Boolean proxyDetected;

    /**
     * Indicates whether the device details mismatch expected patterns.
     */
    private Boolean deviceMismatch;

    /**
     * Indicates whether the location details mismatch expected patterns.
     */
    private Boolean locationMismatch;

    /**
     * Indicates whether the merchant is blacklisted.
     */
    private Boolean blacklistedMerchant;

    /**
     * Indicates whether the device is blacklisted.
     */
    private Boolean blacklistedDevice;

    /**
     * Indicates whether the IP address is blacklisted.
     */
    private Boolean blacklistedIp;

    /**
     * Aggregated risk score between 0 and 100.
     */
    @Min(value = 0, message = "Risk score must be at least 0")
    @Max(value = 100, message = "Risk score must be at most 100")
    private Integer riskScore;

    /**
     * Suspicious pattern labels detected during analysis.
     */
    @Builder.Default
    private List<String> suspiciousPatterns = new ArrayList<>();
}

// Made with Bob