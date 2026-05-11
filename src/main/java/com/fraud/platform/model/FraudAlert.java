package com.fraud.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fraud alert model for dashboard notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {

    /**
     * Alert severity levels.
     */
    public enum Severity {
        CRITICAL,   // High-risk fraud detected (score >= 70)
        HIGH,       // Moderate-risk requiring review (score 50-69)
        MEDIUM,     // Low-risk but flagged (score 30-49)
        LOW         // Informational (score < 30)
    }

    /**
     * Alert type.
     */
    public enum AlertType {
        FRAUD_DETECTED,
        SUSPICIOUS_ACTIVITY,
        VELOCITY_EXCEEDED,
        HIGH_RISK_COUNTRY,
        UNKNOWN_DEVICE,
        AML_WATCHLIST_MATCH,
        BEHAVIOR_ANOMALY
    }

    private String alertId;
    private Severity severity;
    private AlertType alertType;
    private String txnId;
    private String customerId;
    private String message;
    private String detailedMessage;
    private BigDecimal fraudScore;
    private String decision;
    private LocalDateTime timestamp;
    private boolean acknowledged;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;

    /**
     * Create a critical alert.
     */
    public static FraudAlert createCritical(String txnId, String customerId, String message, BigDecimal fraudScore) {
        return FraudAlert.builder()
                .alertId(generateAlertId())
                .severity(Severity.CRITICAL)
                .alertType(AlertType.FRAUD_DETECTED)
                .txnId(txnId)
                .customerId(customerId)
                .message(message)
                .fraudScore(fraudScore)
                .timestamp(LocalDateTime.now())
                .acknowledged(false)
                .build();
    }

    /**
     * Create a high severity alert.
     */
    public static FraudAlert createHigh(String txnId, String customerId, String message, BigDecimal fraudScore) {
        return FraudAlert.builder()
                .alertId(generateAlertId())
                .severity(Severity.HIGH)
                .alertType(AlertType.SUSPICIOUS_ACTIVITY)
                .txnId(txnId)
                .customerId(customerId)
                .message(message)
                .fraudScore(fraudScore)
                .timestamp(LocalDateTime.now())
                .acknowledged(false)
                .build();
    }

    /**
     * Generate unique alert ID.
     */
    private static String generateAlertId() {
        return "ALERT-" + System.currentTimeMillis() + "-" + 
               (int)(Math.random() * 1000);
    }

    /**
     * Acknowledge this alert.
     */
    public void acknowledge(String acknowledgedBy) {
        this.acknowledged = true;
        this.acknowledgedBy = acknowledgedBy;
        this.acknowledgedAt = LocalDateTime.now();
    }
}

// Made with Bob