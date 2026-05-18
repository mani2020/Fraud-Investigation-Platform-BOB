package com.fraud.platform.model.nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested transaction details used for internal fraud analysis payloads.
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInfo {

    /**
     * Transaction amount.
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    /**
     * Transaction currency code.
     */
    @Builder.Default
    private String currency = "USD";

    /**
     * Payment type used for the transaction.
     */
    @NotBlank(message = "Payment type is required")
    private String paymentType;

    /**
     * Merchant category associated with the transaction.
     */
    private String merchantCategory;

    /**
     * Transaction description.
     */
    private String description;

    /**
     * Transaction timestamp.
     */
    private LocalDateTime timestamp;
}

// Made with Bob