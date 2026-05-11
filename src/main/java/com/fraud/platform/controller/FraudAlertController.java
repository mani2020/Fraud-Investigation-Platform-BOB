package com.fraud.platform.controller;

import com.fraud.platform.model.FraudAlert;
import com.fraud.platform.service.FraudNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for fraud alerts and notifications.
 * Provides endpoints for dashboard and investigation purposes.
 */
@RestController
@RequestMapping("/api/fraud-alerts")
@RequiredArgsConstructor
@Slf4j
public class FraudAlertController {

    private final FraudNotificationService notificationService;

    /**
     * Get all recent fraud alerts.
     *
     * @return List of recent alerts
     */
    @GetMapping
    public ResponseEntity<List<FraudAlert>> getAllAlerts() {
        log.info("Fetching all recent alerts");
        List<FraudAlert> alerts = notificationService.getRecentAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alerts by severity.
     *
     * @param severity Alert severity (CRITICAL, HIGH, MEDIUM, LOW)
     * @return List of alerts with specified severity
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<FraudAlert>> getAlertsBySeverity(
            @PathVariable FraudAlert.Severity severity) {
        log.info("Fetching alerts by severity: {}", severity);
        List<FraudAlert> alerts = notificationService.getAlertsBySeverity(severity);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get critical alerts only.
     *
     * @return List of critical alerts
     */
    @GetMapping("/critical")
    public ResponseEntity<List<FraudAlert>> getCriticalAlerts() {
        log.info("Fetching critical alerts");
        List<FraudAlert> alerts = notificationService.getCriticalAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get unacknowledged alerts.
     *
     * @return List of unacknowledged alerts
     */
    @GetMapping("/unacknowledged")
    public ResponseEntity<List<FraudAlert>> getUnacknowledgedAlerts() {
        log.info("Fetching unacknowledged alerts");
        List<FraudAlert> alerts = notificationService.getUnacknowledgedAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alert by ID.
     *
     * @param alertId Alert ID
     * @return Alert details
     */
    @GetMapping("/{alertId}")
    public ResponseEntity<FraudAlert> getAlertById(@PathVariable String alertId) {
        log.info("Fetching alert: {}", alertId);
        FraudAlert alert = notificationService.getAlertById(alertId);
        
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(alert);
    }

    /**
     * Acknowledge an alert.
     *
     * @param alertId Alert ID
     * @param request Acknowledgment request with user info
     * @return Updated alert
     */
    @PutMapping("/{alertId}/acknowledge")
    public ResponseEntity<FraudAlert> acknowledgeAlert(
            @PathVariable String alertId,
            @RequestBody AcknowledgeRequest request) {
        log.info("Acknowledging alert: {} by {}", alertId, request.getAcknowledgedBy());
        
        FraudAlert alert = notificationService.acknowledgeAlert(
                alertId, 
                request.getAcknowledgedBy()
        );
        
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(alert);
    }

    /**
     * Get alert statistics.
     *
     * @return Alert statistics by severity
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<FraudAlert.Severity, Long>> getAlertStats() {
        log.info("Fetching alert statistics");
        Map<FraudAlert.Severity, Long> stats = notificationService.getAlertCountBySeverity();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint.
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fraud Alert API is running");
    }

    /**
     * Request body for acknowledging alerts.
     */
    @lombok.Data
    public static class AcknowledgeRequest {
        private String acknowledgedBy;
    }
}

// Made with Bob