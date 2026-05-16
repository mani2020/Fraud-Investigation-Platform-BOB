package com.fraud.platform.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.model.AgentResult;
import com.fraud.platform.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for fraud investigation details.
 */
@RestController
@RequestMapping("/api/investigation")
@RequiredArgsConstructor
@Slf4j
public class InvestigationController {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get detailed investigation data for a transaction.
     * Includes transaction details, agent analysis, and risk factors.
     * 
     * @param txnId Transaction ID
     * @return Investigation details with agent results
     */
    @GetMapping("/{txnId}")
    public ResponseEntity<Map<String, Object>> getInvestigationDetails(@PathVariable String txnId) {
        log.info("Fetching investigation details for transaction: {}", txnId);
        
        return transactionService.findByTxnId(txnId)
                .map(transaction -> {
                    Map<String, Object> response = new HashMap<>();
                    
                    // Add transaction details
                    response.put("txnId", transaction.getTxnId());
                    response.put("customerId", transaction.getCustomerId());
                    response.put("amount", transaction.getAmount());
                    response.put("merchant", transaction.getMerchant());
                    response.put("country", transaction.getCountry());
                    response.put("deviceId", transaction.getDeviceId());
                    response.put("paymentType", transaction.getPaymentType());
                    response.put("timestamp", transaction.getTimestamp());
                    response.put("status", transaction.getStatus());
                    response.put("fraudScore", transaction.getFraudScore());
                    response.put("fraudDecision", transaction.getFraudDecision());
                    
                    // Get agent results from database (stored as JSON)
                    List<AgentResult> agentResults = deserializeAgentResults(transaction.getAgentResultsJson());
                    response.put("agentResults", agentResults);
                    
                    // Get risk factors from database (stored as JSON)
                    List<String> riskFactors = deserializeRiskFactors(transaction.getRiskFactorsJson());
                    response.put("riskFactors", riskFactors);
                    
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Deserialize agent results from JSON string.
     */
    private List<AgentResult> deserializeAgentResults(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<List<AgentResult>>() {});
        } catch (Exception e) {
            log.error("Error deserializing agent results", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Deserialize risk factors from JSON string.
     */
    private List<String> deserializeRiskFactors(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error deserializing risk factors", e);
            return new ArrayList<>();
        }
    }
}

// Made with Bob