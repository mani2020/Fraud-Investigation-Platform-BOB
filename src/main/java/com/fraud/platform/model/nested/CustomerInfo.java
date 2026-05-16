package com.fraud.platform.model.nested;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nested customer information used for internal fraud analysis payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {

    /**
     * Unique customer identifier.
     */
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /**
     * Customer full name.
     */
    private String customerName;

    /**
     * Customer email address.
     */
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Customer phone number.
     */
    private String phone;

    /**
     * Customer risk level classification.
     */
    private String riskLevel;

    /**
     * Age of the customer account in days.
     */
    @Min(value = 0, message = "Account age must be zero or positive")
    private Integer accountAge;

    /**
     * Total number of transactions associated with the customer.
     */
    private Long totalTransactions;

    /**
     * Average transaction amount for the customer.
     */
    private BigDecimal avgTransactionAmount;

    /**
     * Timestamp of the customer's last activity.
     */
    private LocalDateTime lastActivityDate;
}

// Made with Bob