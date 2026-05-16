package com.fraud.platform.entity;

import com.fraud.platform.entity.converter.*;
import com.fraud.platform.model.nested.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a payment transaction.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_customer_timestamp", columnList = "customer_id, timestamp"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_txn_id", columnList = "txn_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txn_id", nullable = false, unique = true, length = 50)
    private String txnId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 100)
    private String merchant;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore;

    @Column(name = "fraud_decision", length = 20)
    private String fraudDecision;

    @Column(name = "agent_results", columnDefinition = "TEXT")
    private String agentResultsJson;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactorsJson;

    // JSONB columns for nested fraud event data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customer_data", columnDefinition = "jsonb")
    @Convert(converter = CustomerInfoConverter.class)
    private CustomerInfo customerData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "merchant_data", columnDefinition = "jsonb")
    @Convert(converter = MerchantInfoConverter.class)
    private MerchantInfo merchantData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_data", columnDefinition = "jsonb")
    @Convert(converter = DeviceInfoConverter.class)
    private DeviceInfo deviceData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location_data", columnDefinition = "jsonb")
    @Convert(converter = LocationInfoConverter.class)
    private LocationInfo locationData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "behavior_metrics", columnDefinition = "jsonb")
    @Convert(converter = BehaviorMetricsConverter.class)
    private BehaviorMetrics behaviorMetrics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fraud_signals", columnDefinition = "jsonb")
    @Convert(converter = FraudSignalsConverter.class)
    private FraudSignals fraudSignals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = MetadataInfoConverter.class)
    private MetadataInfo metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

// Made with Bob
