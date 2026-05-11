package com.fraud.platform.service;

import com.fraud.platform.model.FraudAlert;
import com.fraud.platform.model.FraudDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service for managing fraud alerts and notifications.
 * Provides real-time alerts for dashboard and investigation purposes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FraudNotificationService {

    // In-memory storage for recent alerts (last 1000 alerts)
    private final CopyOnWriteArrayList<FraudAlert> recentAlerts = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, FraudAlert> alertsById = new ConcurrentHashMap<>();
    private static final int MAX_ALERTS = 1000;

    /**
     * Create and store a fraud alert based on fraud decision.
     *
     * @param decision Fraud decision
     * @return Created fraud alert
     */
    public FraudAlert createAlert(FraudDecision decision) {
        log.info("Creating fraud alert for transaction: {}", decision.getTxnId());

        FraudAlert alert = buildAlertFromDecision(decision);
        storeAlert(alert);

        log.info("Fraud alert created: alertId={}, severity={}, txnId={}", 
                 alert.getAlertId(), alert.getSeverity(), alert.getTxnId());

        return alert;
    }

    /**
     * Create a custom alert.
     *
     * @param severity Alert severity
     * @param txnId Transaction ID
     * @param customerId Customer ID
     * @param message Alert message
     * @param fraudScore Fraud score
     * @return Created fraud alert
     */
    public FraudAlert createCustomAlert(FraudAlert.Severity severity, String txnId, 
                                       String customerId, String message, BigDecimal fraudScore) {
        log.info("Creating custom alert: severity={}, txnId={}", severity, txnId);

        FraudAlert alert = FraudAlert.builder()
                .alertId(generateAlertId())
                .severity(severity)
                .alertType(FraudAlert.AlertType.FRAUD_DETECTED)
                .txnId(txnId)
                .customerId(customerId)
                .message(message)
                .fraudScore(fraudScore)
                .timestamp(java.time.LocalDateTime.now())
                .acknowledged(false)
                .build();

        storeAlert(alert);
        return alert;
    }

    /**
     * Get all recent alerts.
     *
     * @return List of recent alerts
     */
    public List<FraudAlert> getRecentAlerts() {
        return new ArrayList<>(recentAlerts);
    }

    /**
     * Get alerts by severity.
     *
     * @param severity Alert severity
     * @return List of alerts with specified severity
     */
    public List<FraudAlert> getAlertsBySeverity(FraudAlert.Severity severity) {
        return recentAlerts.stream()
                .filter(alert -> alert.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /**
     * Get unacknowledged alerts.
     *
     * @return List of unacknowledged alerts
     */
    public List<FraudAlert> getUnacknowledgedAlerts() {
        return recentAlerts.stream()
                .filter(alert -> !alert.isAcknowledged())
                .collect(Collectors.toList());
    }

    /**
     * Get critical alerts (CRITICAL severity).
     *
     * @return List of critical alerts
     */
    public List<FraudAlert> getCriticalAlerts() {
        return getAlertsBySeverity(FraudAlert.Severity.CRITICAL);
    }

    /**
     * Get alert by ID.
     *
     * @param alertId Alert ID
     * @return Fraud alert or null if not found
     */
    public FraudAlert getAlertById(String alertId) {
        return alertsById.get(alertId);
    }

    /**
     * Acknowledge an alert.
     *
     * @param alertId Alert ID
     * @param acknowledgedBy User who acknowledged
     * @return Updated alert or null if not found
     */
    public FraudAlert acknowledgeAlert(String alertId, String acknowledgedBy) {
        FraudAlert alert = alertsById.get(alertId);
        if (alert != null) {
            alert.acknowledge(acknowledgedBy);
            log.info("Alert acknowledged: alertId={}, by={}", alertId, acknowledgedBy);
        }
        return alert;
    }

    /**
     * Get alert count by severity.
     *
     * @return Map of severity to count
     */
    public java.util.Map<FraudAlert.Severity, Long> getAlertCountBySeverity() {
        return recentAlerts.stream()
                .collect(Collectors.groupingBy(
                        FraudAlert::getSeverity,
                        Collectors.counting()
                ));
    }

    /**
     * Clear all alerts (for testing).
     */
    public void clearAllAlerts() {
        recentAlerts.clear();
        alertsById.clear();
        log.info("All alerts cleared");
    }

    /**
     * Build alert from fraud decision.
     */
    private FraudAlert buildAlertFromDecision(FraudDecision decision) {
        FraudAlert.Severity severity = determineSeverity(decision);
        FraudAlert.AlertType alertType = determineAlertType(decision);
        String message = generateMessage(decision);
        String detailedMessage = generateDetailedMessage(decision);

        return FraudAlert.builder()
                .alertId(generateAlertId())
                .severity(severity)
                .alertType(alertType)
                .txnId(decision.getTxnId())
                .customerId(extractCustomerId(decision))
                .message(message)
                .detailedMessage(detailedMessage)
                .fraudScore(decision.getFinalScore())
                .decision(decision.getDecision())
                .timestamp(decision.getTimestamp())
                .acknowledged(false)
                .build();
    }

    /**
     * Determine alert severity based on fraud decision.
     */
    private FraudAlert.Severity determineSeverity(FraudDecision decision) {
        BigDecimal score = decision.getFinalScore();
        String decisionType = decision.getDecision();

        if ("REJECT".equals(decisionType) || score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return FraudAlert.Severity.CRITICAL;
        } else if ("REVIEW".equals(decisionType) || score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return FraudAlert.Severity.HIGH;
        } else if (score.compareTo(BigDecimal.valueOf(30)) >= 0) {
            return FraudAlert.Severity.MEDIUM;
        } else {
            return FraudAlert.Severity.LOW;
        }
    }

    /**
     * Determine alert type based on fraud decision.
     */
    private FraudAlert.AlertType determineAlertType(FraudDecision decision) {
        // Check agent results for specific alert types
        if (decision.getAgentResults() != null) {
            for (var result : decision.getAgentResults()) {
                if ("GeoAgent".equals(result.getAgentName()) && 
                    result.getRiskScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                    return FraudAlert.AlertType.HIGH_RISK_COUNTRY;
                }
                if ("DeviceAgent".equals(result.getAgentName()) && 
                    result.getRiskScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                    return FraudAlert.AlertType.UNKNOWN_DEVICE;
                }
                if ("AMLAgent".equals(result.getAgentName()) && 
                    result.getRiskScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                    return FraudAlert.AlertType.AML_WATCHLIST_MATCH;
                }
                if ("BehaviorAgent".equals(result.getAgentName()) && 
                    result.getRiskScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                    return FraudAlert.AlertType.BEHAVIOR_ANOMALY;
                }
            }
        }

        return "REJECT".equals(decision.getDecision()) ? 
               FraudAlert.AlertType.FRAUD_DETECTED : 
               FraudAlert.AlertType.SUSPICIOUS_ACTIVITY;
    }

    /**
     * Generate alert message.
     */
    private String generateMessage(FraudDecision decision) {
        String decisionType = decision.getDecision();
        BigDecimal score = decision.getFinalScore();

        return switch (decisionType) {
            case "REJECT" -> String.format("Fraud detected - Score: %.1f", score);
            case "REVIEW" -> String.format("Suspicious activity - Score: %.1f", score);
            default -> String.format("Transaction flagged - Score: %.1f", score);
        };
    }

    /**
     * Generate detailed alert message.
     */
    private String generateDetailedMessage(FraudDecision decision) {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction ").append(decision.getTxnId())
          .append(" flagged with fraud score ").append(decision.getFinalScore())
          .append(". Decision: ").append(decision.getDecision()).append(". ");

        if (decision.getAllReasons() != null && !decision.getAllReasons().isEmpty()) {
            sb.append("Reasons: ");
            sb.append(String.join(", ", decision.getAllReasons().subList(
                    0, Math.min(3, decision.getAllReasons().size()))));
        }

        return sb.toString();
    }

    /**
     * Extract customer ID from fraud decision.
     */
    private String extractCustomerId(FraudDecision decision) {
        // Customer ID would typically be in the decision or need to be passed separately
        // For now, return null - can be enhanced later
        return null;
    }

    /**
     * Store alert in memory.
     */
    private void storeAlert(FraudAlert alert) {
        // Add to recent alerts list
        recentAlerts.add(0, alert); // Add at beginning for most recent first

        // Maintain max size
        if (recentAlerts.size() > MAX_ALERTS) {
            FraudAlert removed = recentAlerts.remove(recentAlerts.size() - 1);
            alertsById.remove(removed.getAlertId());
        }

        // Add to map for quick lookup
        alertsById.put(alert.getAlertId(), alert);
    }

    /**
     * Generate unique alert ID.
     */
    private String generateAlertId() {
        return "ALERT-" + System.currentTimeMillis() + "-" + 
               (int)(Math.random() * 1000);
    }
}

// Made with Bob