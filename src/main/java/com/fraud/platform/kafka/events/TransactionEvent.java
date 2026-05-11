package com.fraud.platform.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka event for transaction fraud analysis.
 * Published to fraud-transactions topic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    private Long transactionId;
    private String txnId;
    private String customerId;
    private BigDecimal amount;
    private String merchant;
    private String country;
    private String deviceId;
    private String paymentType;
    private LocalDateTime timestamp;
    private String status;
    private LocalDateTime eventTime;
}

// Made with Bob
