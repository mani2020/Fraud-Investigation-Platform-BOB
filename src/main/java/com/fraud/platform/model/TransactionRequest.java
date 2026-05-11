package com.fraud.platform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for submitting a payment transaction for fraud detection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Merchant is required")
    private String merchant;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Payment type is required")
    private String paymentType;

    private LocalDateTime timestamp;
}

// Made with Bob
