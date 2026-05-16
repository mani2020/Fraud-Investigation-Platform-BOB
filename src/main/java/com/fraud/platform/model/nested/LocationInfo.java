package com.fraud.platform.model.nested;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Nested location details used for internal fraud analysis payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfo {

    /**
     * Two-character ISO country code.
     */
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be a 2-character code")
    private String country;

    /**
     * City where the transaction originated.
     */
    private String city;

    /**
     * Region or state where the transaction originated.
     */
    private String region;

    /**
     * Latitude coordinate between -90.0 and 90.0.
     */
    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90.0")
    private BigDecimal latitude;

    /**
     * Longitude coordinate between -180.0 and 180.0.
     */
    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180.0")
    private BigDecimal longitude;

    /**
     * IP address associated with the location context.
     */
    private String ipAddress;

    /**
     * Time zone associated with the location.
     */
    private String timezone;
}

// Made with Bob