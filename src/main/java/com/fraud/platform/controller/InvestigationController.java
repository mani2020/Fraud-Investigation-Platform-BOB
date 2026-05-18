package com.fraud.platform.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fraud.platform.service.ICAContextService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for fraud investigation details.
 */
@RestController
@RequestMapping("/api/investigation")
@RequiredArgsConstructor
public class InvestigationController {
    private final ICAContextService icaContextService;

    @GetMapping("/customer/{customerId}/patterns")
    public ResponseEntity<Map<String, Object>> getCustomerFraudPatterns(
            @PathVariable String customerId) {
        Map<String, Object> patterns = icaContextService.findCustomerFraudPatterns(customerId);
        return ResponseEntity.ok(patterns);
    }

    @GetMapping("/device/{deviceId}/fraud-ring")
    public ResponseEntity<Map<String, Object>> detectDeviceFraudRing(
            @PathVariable String deviceId) {
        Map<String, Object> fraudRing = icaContextService.findDeviceFraudRing(deviceId);
        return ResponseEntity.ok(fraudRing);
    }

    @GetMapping("/transaction/{txnId}/explain")
    public ResponseEntity<Map<String, Object>> explainDecision(
            @PathVariable String txnId) {
        Map<String, Object> explanation = icaContextService.getDecisionExplainability(txnId);
        return ResponseEntity.ok(explanation);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFraudKnowledge(
            @RequestParam String query) {
        Map<String, Object> results = icaContextService.vectorSearch(query);
        return ResponseEntity.ok(results);
    }
}

// Made with Bob