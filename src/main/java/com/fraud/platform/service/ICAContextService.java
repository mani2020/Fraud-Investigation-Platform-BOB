package com.fraud.platform.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fraud.platform.config.ICAConfig;
import com.fraud.platform.entity.Transaction;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.model.FraudDecision;
import com.fraud.platform.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ICAContextService {

    private final ICAConfig icaConfig;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send transaction data to ICA Context Studio.
     */
    public void ingestTransaction(CanonicalFraudEvent event,
            FraudDecision decision) {

        try {

            // Validate configuration
            if (icaConfig.getContextId() == null || icaConfig.getContextId().isEmpty()) {
                log.error("❌ ICA_CONTEXT_ID is not configured. Skipping ingestion for txn: {}",
                        event.getTxnId());
                return;
            }

            if (icaConfig.getApiKey() == null || icaConfig.getApiKey().isEmpty()) {
                log.error("❌ ICA_CONTEXT_API_KEY is not configured. Skipping ingestion for txn: {}",
                        event.getTxnId());
                return;
            }

            String url = icaConfig.getBaseUrl()
                    + "/context-broker/post-events";

            Map<String, Object> payload = new HashMap<>();

            payload.put("context_id",
                    icaConfig.getContextId());

            payload.put("event_type",
                    "fraudTransactionAnalysis");

            Map<String, Object> eventData = new HashMap<>();

            eventData.put("transaction", event);
            eventData.put("fraud_decision", decision);
            eventData.put("schema_version",
                    event.getSchemaVersion());

            payload.put("payload", eventData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key",
                    icaConfig.getApiKey());

            // Log request details
            log.debug("Sending ICA ingestion request: url={}, contextId={}, txnId={}",
                    url, icaConfig.getContextId(), event.getTxnId());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class);

            // Enhanced response validation
            if (response.getStatusCode() == HttpStatus.OK ||
                    response.getStatusCode() == HttpStatus.CREATED) {
                log.info("✅ Transaction {} successfully ingested to ICA Context Studio. Status: {}",
                        event.getTxnId(), response.getStatusCode());
                log.debug("ICA Response: {}", response.getBody());
            } else {
                log.warn("⚠️ Unexpected response from ICA: status={}, body={}",
                        response.getStatusCode(), response.getBody());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ ICA ingestion failed for txn: {} - HTTP Error: {} {}",
                    event.getTxnId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("❌ ICA ingestion failed for txn: {} - Network Error: Cannot reach ICA Context Studio at {}",
                    event.getTxnId(), icaConfig.getBaseUrl(), e);
        } catch (Exception e) {
            log.error("❌ ICA ingestion failed for txn: {} - Unexpected Error",
                    event.getTxnId(), e);
        }
    }

    /**
     * Enhanced investigation with ICA Context Studio.
     * Combines PostgreSQL transaction data (v2 with nested JSONB fields)
     * with deep insights from ICA graph and vector queries.
     *
     * @param txnId Transaction ID to investigate
     * @return Combined investigation data with transaction details and ICA insights
     */
    public Map<String, Object> investigateTransaction(String txnId) {

        Map<String, Object> investigation = new HashMap<>();

        try {

            Optional<Transaction> optionalTransaction = transactionRepository.findByTxnId(txnId);

            if (optionalTransaction.isEmpty()) {

                log.warn("Transaction not found: {}", txnId);

                investigation.put(
                        "error",
                        "Transaction not found");

                return investigation;
            }

            Transaction transaction = optionalTransaction.get();

            /*
             * Build transaction response
             */
            Map<String, Object> transactionData = buildTransactionData(transaction);

            investigation.put("transaction", transactionData);

            /*
             * Fetch ICA insights
             */
            enrichInvestigationData(
                    investigation,
                    transaction,
                    txnId);

            investigation.put(
                    "investigationStatus",
                    "SUCCESS");

            log.info(
                    "Investigation completed for transaction: {}",
                    txnId);

        } catch (Exception e) {

            log.error(
                    "Failed to investigate transaction: {}",
                    txnId,
                    e);

            investigation.put(
                    "investigationStatus",
                    "FAILED");

            investigation.put(
                    "error",
                    "Investigation failed: " + e.getMessage());
        }

        return investigation;
    }

    /**
     * Build transaction data response.
     */
    private Map<String, Object> buildTransactionData(
            Transaction transaction) {

        Map<String, Object> transactionData = new HashMap<>();

        transactionData.put("txnId", transaction.getTxnId());
        transactionData.put("customerId", transaction.getCustomerId());
        transactionData.put("amount", transaction.getAmount());
        transactionData.put("merchant", transaction.getMerchant());
        transactionData.put("country", transaction.getCountry());
        transactionData.put("deviceId", transaction.getDeviceId());
        transactionData.put("paymentType", transaction.getPaymentType());
        transactionData.put("timestamp", transaction.getTimestamp());
        transactionData.put("status", transaction.getStatus());
        transactionData.put("fraudScore", transaction.getFraudScore());
        transactionData.put("fraudDecision", transaction.getFraudDecision());

        /*
         * Nested JSONB context
         */
        transactionData.put(
                "customerData",
                transaction.getCustomerData());

        transactionData.put(
                "merchantData",
                transaction.getMerchantData());

        transactionData.put(
                "deviceData",
                transaction.getDeviceData());

        transactionData.put(
                "locationData",
                transaction.getLocationData());

        transactionData.put(
                "behaviorMetrics",
                transaction.getBehaviorMetrics());

        transactionData.put(
                "fraudSignals",
                transaction.getFraudSignals());

        transactionData.put(
                "metadata",
                transaction.getMetadata());

        return transactionData;
    }

    /**
     * Enrich investigation with ICA insights.
     */
    private void enrichInvestigationData(
            Map<String, Object> investigation,
            Transaction transaction,
            String txnId) {

        /*
         * ICA explainability
         */
        try {

            Map<String, Object> icaInsights = getDecisionExplainability(txnId);

            investigation.put(
                    "ica_insights",
                    icaInsights);

        } catch (Exception ex) {

            log.error(
                    "Failed to fetch ICA explainability for txn: {}",
                    txnId,
                    ex);

            investigation.put(
                    "ica_insights_error",
                    ex.getMessage());
        }

        /*
         * Customer fraud patterns
         */
        if (transaction.getCustomerId() != null
                && !transaction.getCustomerId().isBlank()) {

            try {

                Map<String, Object> customerPatterns = findCustomerFraudPatterns(
                        transaction.getCustomerId());

                investigation.put(
                        "customer_patterns",
                        customerPatterns);

            } catch (Exception ex) {

                log.error(
                        "Failed to fetch customer patterns for txn: {}",
                        txnId,
                        ex);

                investigation.put(
                        "customer_patterns_error",
                        ex.getMessage());
            }
        }

        /*
         * Device fraud ring analysis
         */
        if (transaction.getDeviceId() != null
                && !transaction.getDeviceId().isBlank()) {

            try {

                Map<String, Object> deviceFraudRing = findDeviceFraudRing(
                        transaction.getDeviceId());

                investigation.put(
                        "device_fraud_ring",
                        deviceFraudRing);

            } catch (Exception ex) {

                log.error(
                        "Failed to fetch device fraud ring for txn: {}",
                        txnId,
                        ex);

                investigation.put(
                        "device_fraud_ring_error",
                        ex.getMessage());
            }
        }
    }

    /**
     * Find fraud patterns for customer.
     */
    public Map<String, Object> findCustomerFraudPatterns(
            String customerId) {

        try {

            String url = icaConfig.getBaseUrl()
                    + "/context-broker/graph-query";

            Map<String, Object> payload = new HashMap<>();

            payload.put("context_id",
                    icaConfig.getContextId());

            payload.put("AgentPersona",
                    "FraudInvestigator");

            payload.put(
                    "query",
                    "Find all fraud patterns and suspicious transactions for customer "
                            + customerId);

            payload.put("max_depth", 1);
            payload.put("limit", 5);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key",
                    icaConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to query ICA customer patterns", e);
            return new HashMap<>();
        }
    }

    /**
     * Detect fraud ring using device graph traversal.
     */
    public Map<String, Object> findDeviceFraudRing(
            String deviceId) {

        try {

            String url = icaConfig.getBaseUrl()
                    + "/context-broker/graph-query";

            Map<String, Object> payload = new HashMap<>();

            payload.put("context_id",
                    icaConfig.getContextId());

            payload.put("AgentPersona",
                    "FraudInvestigator");

            payload.put(
                    "query",
                    "Find all customers and transactions using device "
                            + deviceId);

            payload.put("max_depth", 2);
            payload.put("limit", 20);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key",
                    icaConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to query ICA device fraud ring", e);
            return new HashMap<>();
        }
    }

    /**
     * Explain fraud decision.
     */
    public Map<String, Object> getDecisionExplainability(
            String txnId) {

        try {

            String url = icaConfig.getBaseUrl()
                    + "/context-broker/hybrid-query";

            Map<String, Object> payload = new HashMap<>();

            payload.put("context_id",
                    icaConfig.getContextId());

            payload.put("AgentPersona",
                    "FraudInvestigator");

            payload.put(
                    "query",
                    "Explain the fraud decision for transaction "
                            + txnId
                            + " including all agent reasoning");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key",
                    icaConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get explainability", e);
            return new HashMap<>();
        }
    }

    /**
     * Vector semantic search.
     */
    public Map<String, Object> vectorSearch(String query) {

        try {

            String url = icaConfig.getBaseUrl()
                    + "/context-broker/vector-query";

            Map<String, Object> payload = new HashMap<>();

            payload.put("context_id",
                    icaConfig.getContextId());

            payload.put("AgentPersona",
                    "FraudInvestigator");

            payload.put("query", query);
            payload.put("top_k", 5);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key",
                    icaConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to perform vector search", e);
            return new HashMap<>();
        }
    }
}