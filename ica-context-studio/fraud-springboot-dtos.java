/*
 * Fraud Investigation Platform - Spring Boot DTOs and Entity Mappings
 * 
 * This file contains all entity classes, DTOs, Kafka event models, and agent models
 * for the fraud investigation platform.
 * 
 * Technology Stack:
 * - Spring Boot 3.x
 * - Spring Data JPA
 * - Hibernate 6.x with JSONB support
 * - Jackson for JSON serialization
 * - Lombok for boilerplate reduction
 * - Jakarta Validation
 * - PostgreSQL with JSONB
 * - Kafka JSON serialization
 * 
 * Package Structure:
 * - com.fraud.platform.entity - JPA entities
 * - com.fraud.platform.model - DTOs
 * - com.fraud.platform.kafka.events - Kafka event models
 */

// ============================================================================
// PACKAGE: com.fraud.platform.entity
// JPA Entity Classes with PostgreSQL JSONB Support
// ============================================================================

package com.fraud.platform.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Transaction Entity - Represents a financial transaction with JSONB metadata
 * 
 * Stores transaction details including amount, parties involved, and flexible
 * metadata in JSONB format for extensibility.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_customer_id", columnList = "customer_id"),
    @Index(name = "idx_transaction_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_timestamp", columnList = "transaction_timestamp"),
    @Index(name = "idx_transaction_fraud_score", columnList = "fraud_score")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Transaction ID is required")
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @NotBlank(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;
    
    @NotBlank(message = "Merchant ID is required")
    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @NotBlank(message = "Transaction type is required")
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;
    
    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @NotNull(message = "Transaction timestamp is required")
    @Column(name = "transaction_timestamp", nullable = false)
    private LocalDateTime transactionTimestamp;
    
    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore;
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    @Column(name = "decision", length = 50)
    private String decision;
    
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Type(JsonBinaryType.class)
    @Column(name = "location_data", columnDefinition = "jsonb")
    private Map<String, Object> locationData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "device_fingerprint", columnDefinition = "jsonb")
    private Map<String, Object> deviceFingerprint;
    
    @Type(JsonBinaryType.class)
    @Column(name = "agent_scores", columnDefinition = "jsonb")
    private Map<String, Object> agentScores;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "kafka_offset")
    private Long kafkaOffset;
    
    @Column(name = "kafka_partition")
    private Integer kafkaPartition;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionEntity)) return false;
        TransactionEntity that = (TransactionEntity) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}

// Made with Bob


/**
 * Customer Entity - Represents customer profile with behavioral data
 */
@Entity
@Table(name = "customer_profiles", indexes = {
    @Index(name = "idx_customer_customer_id", columnList = "customer_id"),
    @Index(name = "idx_customer_risk_level", columnList = "risk_level")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Customer ID is required")
    @Column(name = "customer_id", unique = true, nullable = false, length = 100)
    private String customerId;
    
    @Column(name = "name", length = 200)
    private String name;
    
    @Email(message = "Invalid email format")
    @Column(name = "email", length = 200)
    private String email;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    @Column(name = "account_age_days")
    private Integer accountAgeDays;
    
    @Column(name = "total_transactions")
    private Long totalTransactions;
    
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "average_transaction_amount", precision = 19, scale = 2)
    private BigDecimal averageTransactionAmount;
    
    @Column(name = "fraud_incidents")
    private Integer fraudIncidents;
    
    @Type(JsonBinaryType.class)
    @Column(name = "profile_data", columnDefinition = "jsonb")
    private Map<String, Object> profileData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "behavioral_patterns", columnDefinition = "jsonb")
    private Map<String, Object> behavioralPatterns;
    
    @Type(JsonBinaryType.class)
    @Column(name = "kyc_data", columnDefinition = "jsonb")
    private Map<String, Object> kycData;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerEntity)) return false;
        CustomerEntity that = (CustomerEntity) o;
        return Objects.equals(customerId, that.customerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
}

/**
 * Merchant Entity - Represents merchant information
 */
@Entity
@Table(name = "merchants", indexes = {
    @Index(name = "idx_merchant_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_merchant_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Merchant ID is required")
    @Column(name = "merchant_id", unique = true, nullable = false, length = 100)
    private String merchantId;
    
    @NotBlank(message = "Merchant name is required")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "mcc", length = 10)
    private String mcc;
    
    @Column(name = "country", length = 3)
    private String country;
    
    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;
    
    @Column(name = "fraud_rate", precision = 5, scale = 4)
    private BigDecimal fraudRate;
    
    @Column(name = "total_transactions")
    private Long totalTransactions;
    
    @Type(JsonBinaryType.class)
    @Column(name = "merchant_data", columnDefinition = "jsonb")
    private Map<String, Object> merchantData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "location_data", columnDefinition = "jsonb")
    private Map<String, Object> locationData;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MerchantEntity)) return false;
        MerchantEntity that = (MerchantEntity) o;
        return Objects.equals(merchantId, that.merchantId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(merchantId);
    }
}

/**
 * Device Entity - Represents device fingerprint information
 */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_device_id", columnList = "device_id"),
    @Index(name = "idx_device_customer_id", columnList = "customer_id")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Device ID is required")
    @Column(name = "device_id", unique = true, nullable = false, length = 100)
    private String deviceId;
    
    @Column(name = "customer_id", length = 100)
    private String customerId;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "os", length = 50)
    private String os;
    
    @Column(name = "browser", length = 50)
    private String browser;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "is_trusted")
    private Boolean isTrusted;
    
    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;
    
    @Column(name = "first_seen")
    private LocalDateTime firstSeen;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @Column(name = "transaction_count")
    private Long transactionCount;
    
    @Type(JsonBinaryType.class)
    @Column(name = "fingerprint_data", columnDefinition = "jsonb")
    private Map<String, Object> fingerprintData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "location_history", columnDefinition = "jsonb")
    private Map<String, Object> locationHistory;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceEntity)) return false;
        DeviceEntity that = (DeviceEntity) o;
        return Objects.equals(deviceId, that.deviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }
}

/**
 * Fraud Analysis Entity - Represents complete fraud analysis results
 */
@Entity
@Table(name = "fraud_analyses", indexes = {
    @Index(name = "idx_fraud_analysis_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_fraud_analysis_decision", columnList = "decision"),
    @Index(name = "idx_fraud_analysis_timestamp", columnList = "analysis_timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Analysis ID is required")
    @Column(name = "analysis_id", unique = true, nullable = false, length = 100)
    private String analysisId;
    
    @NotBlank(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;
    
    @NotNull(message = "Fraud score is required")
    @DecimalMin(value = "0.0", message = "Fraud score must be >= 0")
    @DecimalMax(value = "100.0", message = "Fraud score must be <= 100")
    @Column(name = "fraud_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal fraudScore;
    
    @NotBlank(message = "Risk level is required")
    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;
    
    @NotBlank(message = "Decision is required")
    @Column(name = "decision", nullable = false, length = 50)
    private String decision;
    
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;
    
    @NotNull(message = "Analysis timestamp is required")
    @Column(name = "analysis_timestamp", nullable = false)
    private LocalDateTime analysisTimestamp;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Type(JsonBinaryType.class)
    @Column(name = "agent_results", columnDefinition = "jsonb")
    private Map<String, Object> agentResults;
    
    @Type(JsonBinaryType.class)
    @Column(name = "risk_signals", columnDefinition = "jsonb")
    private Map<String, Object> riskSignals;
    
    @Type(JsonBinaryType.class)
    @Column(name = "explanations", columnDefinition = "jsonb")
    private Map<String, Object> explanations;
    
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FraudAnalysisEntity)) return false;
        FraudAnalysisEntity that = (FraudAnalysisEntity) o;
        return Objects.equals(analysisId, that.analysisId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(analysisId);
    }
}

/**
 * Fraud Decision Entity - Represents final fraud decision
 */
@Entity
@Table(name = "fraud_decisions", indexes = {
    @Index(name = "idx_fraud_decision_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_fraud_decision_decision", columnList = "decision")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDecisionEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Decision ID is required")
    @Column(name = "decision_id", unique = true, nullable = false, length = 100)
    private String decisionId;
    
    @NotBlank(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;
    
    @NotBlank(message = "Decision is required")
    @Column(name = "decision", nullable = false, length = 50)
    private String decision;
    
    @Column(name = "decision_reason", length = 500)
    private String decisionReason;
    
    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore;
    
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "decided_by", length = 100)
    private String decidedBy;
    
    @Column(name = "decision_timestamp")
    private LocalDateTime decisionTimestamp;
    
    @Column(name = "review_required")
    private Boolean reviewRequired;
    
    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;
    
    @Column(name = "review_timestamp")
    private LocalDateTime reviewTimestamp;
    
    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;
    
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FraudDecisionEntity)) return false;
        FraudDecisionEntity that = (FraudDecisionEntity) o;
        return Objects.equals(decisionId, that.decisionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(decisionId);
    }
}

/**
 * Audit Log Entity - Represents audit trail for compliance
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_log_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_log_entity_id", columnList = "entity_id"),
    @Index(name = "idx_audit_log_action", columnList = "action"),
    @Index(name = "idx_audit_log_timestamp", columnList = "timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Entity type is required")
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;
    
    @NotBlank(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;
    
    @NotBlank(message = "Action is required")
    @Column(name = "action", nullable = false, length = 50)
    private String action;
    
    @Column(name = "user_id", length = 100)
    private String userId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Type(JsonBinaryType.class)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;
    
    @Type(JsonBinaryType.class)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;
    
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLogEntity)) return false;
        AuditLogEntity that = (AuditLogEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


/**
 * Kafka Event Entity - Represents Kafka events for replay and debugging
 */
@Entity
@Table(name = "kafka_events", indexes = {
    @Index(name = "idx_kafka_event_topic", columnList = "topic"),
    @Index(name = "idx_kafka_event_key", columnList = "event_key"),
    @Index(name = "idx_kafka_event_timestamp", columnList = "event_timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEventEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Topic is required")
    @Column(name = "topic", nullable = false, length = 200)
    private String topic;
    
    @Column(name = "event_key", length = 200)
    private String eventKey;
    
    @NotBlank(message = "Event type is required")
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @NotNull(message = "Event timestamp is required")
    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;
    
    @Column(name = "partition")
    private Integer partition;
    
    @Column(name = "offset")
    private Long offset;
    
    @Type(JsonBinaryType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;
    
    @Type(JsonBinaryType.class)
    @Column(name = "headers", columnDefinition = "jsonb")
    private Map<String, Object> headers;
    
    @Column(name = "processing_status", length = 50)
    private String processingStatus;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KafkaEventEntity)) return false;
        KafkaEventEntity that = (KafkaEventEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

/**
 * Agent Execution Entity - Represents individual agent execution results
 */
@Entity
@Table(name = "agent_executions", indexes = {
    @Index(name = "idx_agent_execution_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_agent_execution_agent_name", columnList = "agent_name"),
    @Index(name = "idx_agent_execution_timestamp", columnList = "execution_timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Execution ID is required")
    @Column(name = "execution_id", unique = true, nullable = false, length = 100)
    private String executionId;
    
    @NotBlank(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;
    
    @NotBlank(message = "Agent name is required")
    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;
    
    @Column(name = "agent_version", length = 20)
    private String agentVersion;
    
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    @Column(name = "execution_status", length = 50)
    private String executionStatus;
    
    @Column(name = "execution_timestamp")
    private LocalDateTime executionTimestamp;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Type(JsonBinaryType.class)
    @Column(name = "input_data", columnDefinition = "jsonb")
    private Map<String, Object> inputData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "output_data", columnDefinition = "jsonb")
    private Map<String, Object> outputData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "signals", columnDefinition = "jsonb")
    private Map<String, Object> signals;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentExecutionEntity)) return false;
        AgentExecutionEntity that = (AgentExecutionEntity) o;
        return Objects.equals(executionId, that.executionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(executionId);
    }
}

/**
 * Risk Signal Entity - Represents detected risk signals
 */
@Entity
@Table(name = "risk_signals", indexes = {
    @Index(name = "idx_risk_signal_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_risk_signal_signal_type", columnList = "signal_type"),
    @Index(name = "idx_risk_signal_severity", columnList = "severity")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskSignalEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Signal ID is required")
    @Column(name = "signal_id", unique = true, nullable = false, length = 100)
    private String signalId;
    
    @NotBlank(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;
    
    @NotBlank(message = "Signal type is required")
    @Column(name = "signal_type", nullable = false, length = 100)
    private String signalType;
    
    @NotBlank(message = "Severity is required")
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;
    
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "detected_by", length = 100)
    private String detectedBy;
    
    @Column(name = "detected_at")
    private LocalDateTime detectedAt;
    
    @Type(JsonBinaryType.class)
    @Column(name = "signal_data", columnDefinition = "jsonb")
    private Map<String, Object> signalData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "context", columnDefinition = "jsonb")
    private Map<String, Object> context;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RiskSignalEntity)) return false;
        RiskSignalEntity that = (RiskSignalEntity) o;
        return Objects.equals(signalId, that.signalId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(signalId);
    }
}

/**
 * Transaction Pattern Entity - Represents detected transaction patterns
 */
@Entity
@Table(name = "transaction_patterns", indexes = {
    @Index(name = "idx_transaction_pattern_customer_id", columnList = "customer_id"),
    @Index(name = "idx_transaction_pattern_pattern_type", columnList = "pattern_type")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPatternEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Pattern ID is required")
    @Column(name = "pattern_id", unique = true, nullable = false, length = 100)
    private String patternId;
    
    @Column(name = "customer_id", length = 100)
    private String customerId;
    
    @NotBlank(message = "Pattern type is required")
    @Column(name = "pattern_type", nullable = false, length = 100)
    private String patternType;
    
    @Column(name = "pattern_name", length = 200)
    private String patternName;
    
    @Column(name = "confidence", precision = 5, scale = 2)
    private BigDecimal confidence;
    
    @Column(name = "frequency")
    private Integer frequency;
    
    @Column(name = "first_detected")
    private LocalDateTime firstDetected;
    
    @Column(name = "last_detected")
    private LocalDateTime lastDetected;
    
    @Type(JsonBinaryType.class)
    @Column(name = "pattern_data", columnDefinition = "jsonb")
    private Map<String, Object> patternData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "statistics", columnDefinition = "jsonb")
    private Map<String, Object> statistics;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionPatternEntity)) return false;
        TransactionPatternEntity that = (TransactionPatternEntity) o;
        return Objects.equals(patternId, that.patternId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patternId);
    }
}

/**
 * Behavioral Profile Entity - Represents customer behavioral profile
 */
@Entity
@Table(name = "behavioral_profiles", indexes = {
    @Index(name = "idx_behavioral_profile_customer_id", columnList = "customer_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehavioralProfileEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "Profile ID is required")
    @Column(name = "profile_id", unique = true, nullable = false, length = 100)
    private String profileId;
    
    @NotBlank(message = "Customer ID is required")
    @Column(name = "customer_id", unique = true, nullable = false, length = 100)
    private String customerId;
    
    @Column(name = "average_transaction_amount", precision = 19, scale = 2)
    private BigDecimal averageTransactionAmount;
    
    @Column(name = "transaction_frequency")
    private Integer transactionFrequency;
    
    @Column(name = "preferred_merchants", length = 1000)
    private String preferredMerchants;
    
    @Column(name = "preferred_categories", length = 1000)
    private String preferredCategories;
    
    @Column(name = "typical_transaction_time", length = 100)
    private String typicalTransactionTime;
    
    @Column(name = "typical_locations", length = 1000)
    private String typicalLocations;
    
    @Type(JsonBinaryType.class)
    @Column(name = "spending_patterns", columnDefinition = "jsonb")
    private Map<String, Object> spendingPatterns;
    
    @Type(JsonBinaryType.class)
    @Column(name = "device_patterns", columnDefinition = "jsonb")
    private Map<String, Object> devicePatterns;
    
    @Type(JsonBinaryType.class)
    @Column(name = "location_patterns", columnDefinition = "jsonb")
    private Map<String, Object> locationPatterns;
    
    @Type(JsonBinaryType.class)
    @Column(name = "temporal_patterns", columnDefinition = "jsonb")
    private Map<String, Object> temporalPatterns;
    
    @Column(name = "profile_confidence", precision = 5, scale = 2)
    private BigDecimal profileConfidence;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @CreationTimestamp
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BehavioralProfileEntity)) return false;
        BehavioralProfileEntity that = (BehavioralProfileEntity) o;
        return Objects.equals(customerId, that.customerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
}

// ============================================================================
// PACKAGE: com.fraud.platform.model
// Data Transfer Objects (DTOs)
// ============================================================================

package com.fraud.platform.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Transaction Request DTO - Request payload for creating transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("transactionId")
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
    
    @JsonProperty("customerId")
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @JsonProperty("merchantId")
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("transactionType")
    @NotBlank(message = "Transaction type is required")
    private String transactionType;
    
    @JsonProperty("timestamp")
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("locationData")
    private Map<String, Object> locationData;
    
    @JsonProperty("deviceFingerprint")
    private Map<String, Object> deviceFingerprint;
}

/**
 * Transaction Response DTO - Response payload for transaction operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("fraudScore")
    private BigDecimal fraudScore;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("decision")
    private String decision;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("agentScores")
    private List<AgentScoreDTO> agentScores;
    
    @JsonProperty("explanations")
    private List<ExplanationDTO> explanations;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Fraud Analysis Request DTO - Request for fraud analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FraudAnalysisRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("transactionId")
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @JsonProperty("forceReanalysis")
    private Boolean forceReanalysis;
    
    @JsonProperty("agentsToRun")
    private List<String> agentsToRun;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Fraud Analysis Response DTO - Response for fraud analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FraudAnalysisResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("analysisId")
    private String analysisId;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("fraudScore")
    private BigDecimal fraudScore;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("decision")
    private String decision;
    
    @JsonProperty("confidenceScore")
    private BigDecimal confidenceScore;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("agentResults")
    private List<AgentScoreDTO> agentResults;
    
    @JsonProperty("riskSignals")
    private List<RiskSignalDTO> riskSignals;
    
    @JsonProperty("explanations")
    private List<ExplanationDTO> explanations;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Agent Score DTO - Represents individual agent scoring result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentScoreDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("agentName")
    private String agentName;
    
    @JsonProperty("score")
    private BigDecimal score;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("confidence")
    private BigDecimal confidence;
    
    @JsonProperty("executionTimeMs")
    private Long executionTimeMs;
    
    @JsonProperty("signals")
    private List<String> signals;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Risk Signal DTO - Represents a detected risk signal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskSignalDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("signalType")
    private String signalType;
    
    @JsonProperty("severity")
    private String severity;
    
    @JsonProperty("score")
    private BigDecimal score;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("detectedBy")
    private String detectedBy;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Fraud Decision DTO - Represents final fraud decision
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FraudDecisionDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("decisionId")
    private String decisionId;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("decision")
    private String decision;
    
    @JsonProperty("decisionReason")
    private String decisionReason;
    
    @JsonProperty("fraudScore")
    private BigDecimal fraudScore;
    
    @JsonProperty("confidenceScore")
    private BigDecimal confidenceScore;
    
    @JsonProperty("decidedBy")
    private String decidedBy;
    
    @JsonProperty("decisionTimestamp")
    private LocalDateTime decisionTimestamp;
    
    @JsonProperty("reviewRequired")
    private Boolean reviewRequired;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Explanation DTO - Represents explainable AI output
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExplanationDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("factor")
    private String factor;
    
    @JsonProperty("impact")
    private String impact;
    
    @JsonProperty("weight")
    private BigDecimal weight;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("evidence")
    private List<String> evidence;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Kafka Event DTO - Represents Kafka event data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KafkaEventDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("eventId")
    private String eventId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("topic")
    private String topic;
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("payload")
    private Map<String, Object> payload;
    
    @JsonProperty("headers")
    private Map<String, String> headers;
}

/**
 * Audit Log DTO - Represents audit log entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("entityType")
    private String entityType;
    
    @JsonProperty("entityId")
    private String entityId;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("oldValue")
    private Map<String, Object> oldValue;
    
    @JsonProperty("newValue")
    private Map<String, Object> newValue;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Customer Profile DTO - Represents customer profile data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfileDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("accountAgeDays")
    private Integer accountAgeDays;
    
    @JsonProperty("totalTransactions")
    private Long totalTransactions;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("averageTransactionAmount")
    private BigDecimal averageTransactionAmount;
    
    @JsonProperty("fraudIncidents")
    private Integer fraudIncidents;
    
    @JsonProperty("profileData")
    private Map<String, Object> profileData;
    
    @JsonProperty("behavioralPatterns")
    private Map<String, Object> behavioralPatterns;
}

/**
 * Merchant Data DTO - Represents merchant information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantDataDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("merchantId")
    private String merchantId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("mcc")
    private String mcc;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("riskScore")
    private BigDecimal riskScore;
    
    @JsonProperty("fraudRate")
    private BigDecimal fraudRate;
    
    @JsonProperty("totalTransactions")
    private Long totalTransactions;
    
    @JsonProperty("merchantData")
    private Map<String, Object> merchantData;
}

/**
 * Device Fingerprint DTO - Represents device fingerprint data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceFingerprintDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("deviceType")
    private String deviceType;
    
    @JsonProperty("os")
    private String os;
    
    @JsonProperty("browser")
    private String browser;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("isTrusted")
    private Boolean isTrusted;
    
    @JsonProperty("riskScore")
    private BigDecimal riskScore;
    
    @JsonProperty("fingerprintData")
    private Map<String, Object> fingerprintData;
}


// ============================================================================
// PACKAGE: com.fraud.platform.kafka.events
// Kafka Event Models
// ============================================================================

package com.fraud.platform.kafka.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base Kafka Event - Base class for all Kafka events with common metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseKafkaEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("eventId")
    private String eventId = UUID.randomUUID().toString();
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("source")
    private String source = "fraud-platform";
    
    @JsonProperty("version")
    private String version = "1.0";
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

/**
 * Transaction Created Event - Published when a new transaction is created
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionCreatedEvent extends BaseKafkaEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("merchantId")
    private String merchantId;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("transactionType")
    private String transactionType;
    
    @JsonProperty("transactionTimestamp")
    private LocalDateTime transactionTimestamp;
    
    @JsonProperty("locationData")
    private Map<String, Object> locationData;
    
    @JsonProperty("deviceFingerprint")
    private Map<String, Object> deviceFingerprint;
    
    @JsonProperty("transactionMetadata")
    private Map<String, Object> transactionMetadata;
    
    public TransactionCreatedEvent(String transactionId, BigDecimal amount, String currency) {
        super();
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        setEventType("TRANSACTION_CREATED");
    }
}

/**
 * Fraud Analysis Started Event - Published when fraud analysis begins
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FraudAnalysisStartedEvent extends BaseKafkaEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("analysisId")
    private String analysisId;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("agentsToRun")
    private java.util.List<String> agentsToRun;
    
    @JsonProperty("analysisStartTime")
    private LocalDateTime analysisStartTime;
    
    public FraudAnalysisStartedEvent(String analysisId, String transactionId) {
        super();
        this.analysisId = analysisId;
        this.transactionId = transactionId;
        this.analysisStartTime = LocalDateTime.now();
        setEventType("FRAUD_ANALYSIS_STARTED");
    }
}

/**
 * Fraud Analysis Completed Event - Published when fraud analysis completes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FraudAnalysisCompletedEvent extends BaseKafkaEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("analysisId")
    private String analysisId;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("fraudScore")
    private BigDecimal fraudScore;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("decision")
    private String decision;
    
    @JsonProperty("confidenceScore")
    private BigDecimal confidenceScore;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("agentResults")
    private Map<String, Object> agentResults;
    
    @JsonProperty("riskSignals")
    private java.util.List<Map<String, Object>> riskSignals;
    
    @JsonProperty("analysisCompletionTime")
    private LocalDateTime analysisCompletionTime;
    
    public FraudAnalysisCompletedEvent(String analysisId, String transactionId, BigDecimal fraudScore, String decision) {
        super();
        this.analysisId = analysisId;
        this.transactionId = transactionId;
        this.fraudScore = fraudScore;
        this.decision = decision;
        this.analysisCompletionTime = LocalDateTime.now();
        setEventType("FRAUD_ANALYSIS_COMPLETED");
    }
}

/**
 * Decision Made Event - Published when a fraud decision is made
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionMadeEvent extends BaseKafkaEvent {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("decisionId")
    private String decisionId;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("decision")
    private String decision;
    
    @JsonProperty("decisionReason")
    private String decisionReason;
    
    @JsonProperty("fraudScore")
    private BigDecimal fraudScore;
    
    @JsonProperty("confidenceScore")
    private BigDecimal confidenceScore;
    
    @JsonProperty("decidedBy")
    private String decidedBy;
    
    @JsonProperty("decisionTimestamp")
    private LocalDateTime decisionTimestamp;
    
    @JsonProperty("reviewRequired")
    private Boolean reviewRequired;
    
    @JsonProperty("explanations")
    private java.util.List<Map<String, Object>> explanations;
    
    public DecisionMadeEvent(String decisionId, String transactionId, String decision) {
        super();
        this.decisionId = decisionId;
        this.transactionId = transactionId;
        this.decision = decision;
        this.decisionTimestamp = LocalDateTime.now();
        setEventType("DECISION_MADE");
    }
}

// ============================================================================
// PACKAGE: com.fraud.platform.model (Agent Models)
// Agent Result Models
// ============================================================================

package com.fraud.platform.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent Result - Base result class for all fraud detection agents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("agentName")
    private String agentName;
    
    @JsonProperty("score")
    private BigDecimal score;
    
    @JsonProperty("riskLevel")
    private String riskLevel;
    
    @JsonProperty("confidence")
    private BigDecimal confidence;
    
    @JsonProperty("executionTimeMs")
    private Long executionTimeMs;
    
    @JsonProperty("signals")
    @Builder.Default
    private List<String> signals = new ArrayList<>();
    
    @JsonProperty("metadata")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
}

/**
 * Risk Agent Result - Result from Risk-based fraud detection agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("amountRisk")
    private BigDecimal amountRisk;
    
    @JsonProperty("velocityRisk")
    private BigDecimal velocityRisk;
    
    @JsonProperty("merchantRisk")
    private BigDecimal merchantRisk;
    
    @JsonProperty("customerRisk")
    private BigDecimal customerRisk;
    
    @JsonProperty("riskFactors")
    @Builder.Default
    private List<String> riskFactors = new ArrayList<>();
    
    @JsonProperty("thresholdExceeded")
    private Boolean thresholdExceeded;
}

/**
 * Geo Agent Result - Result from Geographic fraud detection agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("locationAnomaly")
    private Boolean locationAnomaly;
    
    @JsonProperty("distanceFromLastTransaction")
    private Double distanceFromLastTransaction;
    
    @JsonProperty("impossibleTravel")
    private Boolean impossibleTravel;
    
    @JsonProperty("highRiskCountry")
    private Boolean highRiskCountry;
    
    @JsonProperty("ipLocationMismatch")
    private Boolean ipLocationMismatch;
    
    @JsonProperty("currentLocation")
    private Map<String, Object> currentLocation;
    
    @JsonProperty("previousLocation")
    private Map<String, Object> previousLocation;
}

/**
 * Device Agent Result - Result from Device fingerprint fraud detection agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("deviceTrusted")
    private Boolean deviceTrusted;
    
    @JsonProperty("newDevice")
    private Boolean newDevice;
    
    @JsonProperty("deviceRiskScore")
    private BigDecimal deviceRiskScore;
    
    @JsonProperty("deviceAnomalies")
    @Builder.Default
    private List<String> deviceAnomalies = new ArrayList<>();
    
    @JsonProperty("fingerprintMatch")
    private Boolean fingerprintMatch;
    
    @JsonProperty("deviceChangeDetected")
    private Boolean deviceChangeDetected;
    
    @JsonProperty("deviceInfo")
    private Map<String, Object> deviceInfo;
}

/**
 * AML Agent Result - Result from Anti-Money Laundering detection agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AMLAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("amlRisk")
    private String amlRisk;
    
    @JsonProperty("sanctionListMatch")
    private Boolean sanctionListMatch;
    
    @JsonProperty("pepMatch")
    private Boolean pepMatch;
    
    @JsonProperty("structuringDetected")
    private Boolean structuringDetected;
    
    @JsonProperty("unusualPatternDetected")
    private Boolean unusualPatternDetected;
    
    @JsonProperty("amlFlags")
    @Builder.Default
    private List<String> amlFlags = new ArrayList<>();
    
    @JsonProperty("complianceScore")
    private BigDecimal complianceScore;
}

/**
 * Behavior Agent Result - Result from Behavioral analysis agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BehaviorAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("behaviorAnomaly")
    private Boolean behaviorAnomaly;
    
    @JsonProperty("deviationScore")
    private BigDecimal deviationScore;
    
    @JsonProperty("unusualAmount")
    private Boolean unusualAmount;
    
    @JsonProperty("unusualMerchant")
    private Boolean unusualMerchant;
    
    @JsonProperty("unusualTime")
    private Boolean unusualTime;
    
    @JsonProperty("unusualFrequency")
    private Boolean unusualFrequency;
    
    @JsonProperty("behaviorPatterns")
    @Builder.Default
    private List<String> behaviorPatterns = new ArrayList<>();
    
    @JsonProperty("profileMatch")
    private BigDecimal profileMatch;
}

/**
 * Decision Agent Result - Result from Decision-making agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("finalDecision")
    private String finalDecision;
    
    @JsonProperty("decisionReason")
    private String decisionReason;
    
    @JsonProperty("aggregatedScore")
    private BigDecimal aggregatedScore;
    
    @JsonProperty("reviewRequired")
    private Boolean reviewRequired;
    
    @JsonProperty("actionRecommended")
    private String actionRecommended;
    
    @JsonProperty("decisionFactors")
    @Builder.Default
    private List<Map<String, Object>> decisionFactors = new ArrayList<>();
    
    @JsonProperty("thresholds")
    private Map<String, BigDecimal> thresholds;
}

/**
 * Explainability Agent Result - Result from Explainability agent
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExplainabilityAgentResult extends AgentResult {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("explanations")
    @Builder.Default
    private List<ExplanationDTO> explanations = new ArrayList<>();
    
    @JsonProperty("topFactors")
    @Builder.Default
    private List<String> topFactors = new ArrayList<>();
    
    @JsonProperty("featureImportance")
    private Map<String, BigDecimal> featureImportance;
    
    @JsonProperty("decisionPath")
    @Builder.Default
    private List<String> decisionPath = new ArrayList<>();
    
    @JsonProperty("confidenceBreakdown")
    private Map<String, BigDecimal> confidenceBreakdown;
    
    @JsonProperty("humanReadableExplanation")
    private String humanReadableExplanation;
}

// ============================================================================
// Entity Mapper Utilities
// ============================================================================

package com.fraud.platform.util;

import com.fraud.platform.entity.*;
import com.fraud.platform.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity Mapper - Utility class for mapping between entities and DTOs
 */
@Component
public class EntityMapper {
    
    /**
     * Convert TransactionEntity to TransactionResponse
     */
    public TransactionResponse toTransactionResponse(TransactionEntity entity) {
        if (entity == null) return null;
        
        return TransactionResponse.builder()
            .transactionId(entity.getTransactionId())
            .status(entity.getStatus())
            .fraudScore(entity.getFraudScore())
            .riskLevel(entity.getRiskLevel())
            .decision(entity.getDecision())
            .processingTimeMs(entity.getProcessingTimeMs())
            .timestamp(entity.getTransactionTimestamp())
            .metadata(entity.getMetadata())
            .build();
    }
    
    /**
     * Convert TransactionRequest to TransactionEntity
     */
    public TransactionEntity toTransactionEntity(TransactionRequest request) {
        if (request == null) return null;
        
        return TransactionEntity.builder()
            .transactionId(request.getTransactionId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .customerId(request.getCustomerId())
            .merchantId(request.getMerchantId())
            .deviceId(request.getDeviceId())
            .transactionType(request.getTransactionType())
            .transactionTimestamp(request.getTimestamp())
            .status("PENDING")
            .metadata(request.getMetadata())
            .locationData(request.getLocationData())
            .deviceFingerprint(request.getDeviceFingerprint())
            .deleted(false)
            .build();
    }
    
    /**
     * Convert FraudAnalysisEntity to FraudAnalysisResponse
     */
    public FraudAnalysisResponse toFraudAnalysisResponse(FraudAnalysisEntity entity) {
        if (entity == null) return null;
        
        return FraudAnalysisResponse.builder()
            .analysisId(entity.getAnalysisId())
            .transactionId(entity.getTransactionId())
            .fraudScore(entity.getFraudScore())
            .riskLevel(entity.getRiskLevel())
            .decision(entity.getDecision())
            .confidenceScore(entity.getConfidenceScore())
            .processingTimeMs(entity.getProcessingTimeMs())
            .timestamp(entity.getAnalysisTimestamp())
            .metadata(entity.getMetadata())
            .build();
    }
    
    /**
     * Convert CustomerEntity to CustomerProfileDTO
     */
    public CustomerProfileDTO toCustomerProfileDTO(CustomerEntity entity) {
        if (entity == null) return null;
        
        return CustomerProfileDTO.builder()
            .customerId(entity.getCustomerId())
            .name(entity.getName())
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .riskLevel(entity.getRiskLevel())
            .accountAgeDays(entity.getAccountAgeDays())
            .totalTransactions(entity.getTotalTransactions())
            .totalAmount(entity.getTotalAmount())
            .averageTransactionAmount(entity.getAverageTransactionAmount())
            .fraudIncidents(entity.getFraudIncidents())
            .profileData(entity.getProfileData())
            .behavioralPatterns(entity.getBehavioralPatterns())
            .build();
    }
    
    /**
     * Convert MerchantEntity to MerchantDataDTO
     */
    public MerchantDataDTO toMerchantDataDTO(MerchantEntity entity) {
        if (entity == null) return null;
        
        return MerchantDataDTO.builder()
            .merchantId(entity.getMerchantId())
            .name(entity.getName())
            .category(entity.getCategory())
            .mcc(entity.getMcc())
            .country(entity.getCountry())
            .riskScore(entity.getRiskScore())
            .fraudRate(entity.getFraudRate())
            .totalTransactions(entity.getTotalTransactions())
            .merchantData(entity.getMerchantData())
            .build();
    }
    
    /**
     * Convert DeviceEntity to DeviceFingerprintDTO
     */
    public DeviceFingerprintDTO toDeviceFingerprintDTO(DeviceEntity entity) {
        if (entity == null) return null;
        
        return DeviceFingerprintDTO.builder()
            .deviceId(entity.getDeviceId())
            .deviceType(entity.getDeviceType())
            .os(entity.getOs())
            .browser(entity.getBrowser())
            .ipAddress(entity.getIpAddress())
            .isTrusted(entity.getIsTrusted())
            .riskScore(entity.getRiskScore())
            .fingerprintData(entity.getFingerprintData())
            .build();
    }
}

/*
 * END OF FILE
 * 
 * This file contains comprehensive Spring Boot DTOs and entity mappings for the
 * Fraud Investigation Platform, including:
 * 
 * - 12 JPA Entity classes with JSONB support
 * - 12 DTO classes for API communication
 * - 4 Kafka event models with base event class
 * - 7 Agent result models (base + 6 specialized agents)
 * - Entity mapper utilities for conversions
 * 
 * All classes are production-ready with:
 * - Proper JPA annotations
 * - JSONB type handling
 * - Jackson serialization
 * - Lombok annotations
 * - Jakarta validation
 * - Comprehensive JavaDoc
 * - Builder pattern support
 * - Equals/hashCode implementations
 * 
 * Compatible with:
 * - Spring Boot 3.x
 * - Spring Data JPA
 * - Hibernate 6.x
 * - PostgreSQL JSONB
 * - Kafka JSON serialization
 */
