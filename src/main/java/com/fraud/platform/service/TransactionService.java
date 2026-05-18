package com.fraud.platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.entity.Transaction;
import com.fraud.platform.kafka.KafkaProducerService;
import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.AgentResult;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.model.TransactionRequest;
import com.fraud.platform.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing payment transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    /**
     * Create a new transaction from request.
     */
    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        log.info("Creating transaction: txnId={}, customerId={}, amount={}",
                request.getTxnId(), request.getCustomerId(), request.getAmount());

        // Check if transaction already exists
        Optional<Transaction> existing = transactionRepository.findByTxnId(request.getTxnId());
        if (existing.isPresent()) {
            log.warn("Transaction already exists: txnId={}", request.getTxnId());
            throw new IllegalArgumentException("Transaction with ID " + request.getTxnId() + " already exists");
        }

        // Create transaction entity
        Transaction transaction = Transaction.builder()
                .txnId(request.getTxnId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .merchant(request.getMerchant())
                .country(request.getCountry())
                .deviceId(request.getDeviceId())
                .paymentType(request.getPaymentType())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : java.time.LocalDateTime.now())
                .status("PENDING")
                .build();

        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully: id={}, txnId={}",
                savedTransaction.getId(), savedTransaction.getTxnId());

        // Publish to Kafka for fraud detection v1
        publishTransactionEvent(savedTransaction);

        return savedTransaction;
    }

    @Transactional
    public Transaction createTransactionV2(
            TransactionRequest request,
            CanonicalFraudEvent canonicalEvent) {

        log.info(
                "Creating V2 transaction: txnId={}",
                request.getTxnId());

        Optional<Transaction> existing = transactionRepository.findByTxnId(
                request.getTxnId());

        if (existing.isPresent()) {

            throw new IllegalArgumentException(
                    "Transaction already exists");
        }

        Transaction transaction = Transaction.builder()
                .txnId(request.getTxnId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .merchant(request.getMerchant())
                .country(request.getCountry())
                .deviceId(request.getDeviceId())
                .paymentType(request.getPaymentType())
                .timestamp(
                        request.getTimestamp() != null
                                ? request.getTimestamp()
                                : LocalDateTime.now())
                .status("PENDING")
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        /*
         * Persist FULL canonical payload
         */
        updateWithCanonicalEvent(
                savedTransaction.getTxnId(),
                canonicalEvent);

        /*
         * Publish ONLY V2 event
         */
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        publishTransactionEventV2(
                                canonicalEvent);
                        log.info(
                                "V2 transaction created successfully: txnId={}",
                                savedTransaction.getTxnId());
                    }
                });

        return savedTransaction;
    }

    /**
     * Find transaction by transaction ID.
     */
    public Optional<Transaction> findByTxnId(String txnId) {
        log.debug("Finding transaction by txnId: {}", txnId);
        return transactionRepository.findByTxnId(txnId);
    }

    /**
     * Find transaction by database ID.
     */
    public Optional<Transaction> findById(Long id) {
        log.debug("Finding transaction by id: {}", id);
        return transactionRepository.findById(id);
    }

    /**
     * Find all transactions for a customer.
     */
    public List<Transaction> findByCustomerId(String customerId) {
        log.debug("Finding transactions for customerId: {}", customerId);
        return transactionRepository.findByCustomerIdOrderByTimestampDesc(customerId);
    }

    /**
     * Find all transactions.
     */
    public List<Transaction> findAll() {
        log.debug("Finding all transactions");
        return transactionRepository.findAll();
    }

    /**
     * Update transaction status.
     */
    @Transactional
    public Transaction updateStatus(String txnId, String status) {
        log.info("Updating transaction status: txnId={}, status={}", txnId, status);

        Transaction transaction = transactionRepository.findByTxnId(txnId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + txnId));

        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    /**
     * Update fraud decision and score.
     */
    @Transactional
    public Transaction updateFraudDecision(String txnId, String decision, java.math.BigDecimal score) {
        log.info("Updating fraud decision: txnId={}, decision={}, score={}", txnId, decision, score);

        Transaction transaction = transactionRepository.findByTxnId(txnId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + txnId));

        transaction.setFraudDecision(decision);
        transaction.setFraudScore(score);
        transaction.setStatus("PROCESSED");

        return transactionRepository.save(transaction);
    }

    /**
     * Update fraud decision with agent results and risk factors.
     */
    @Transactional
    public Transaction updateFraudDecisionWithDetails(String txnId, String decision, java.math.BigDecimal score,
            List<AgentResult> agentResults, List<String> riskFactors) {
        log.info("Updating fraud decision with details: txnId={}, decision={}, score={}", txnId, decision, score);

        Transaction transaction = transactionRepository.findByTxnId(txnId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + txnId));

        transaction.setFraudDecision(decision);
        transaction.setFraudScore(score);
        transaction.setStatus("PROCESSED");

        // Serialize agent results and risk factors to JSON
        try {
            if (agentResults != null && !agentResults.isEmpty()) {
                transaction.setAgentResultsJson(objectMapper.writeValueAsString(agentResults));
            }
            if (riskFactors != null && !riskFactors.isEmpty()) {
                transaction.setRiskFactorsJson(objectMapper.writeValueAsString(riskFactors));
            }
        } catch (Exception e) {
            log.error("Error serializing fraud details for transaction: {}", txnId, e);
        }

        return transactionRepository.save(transaction);
    }

    /**
     * Update transaction with nested fraud event data from CanonicalFraudEvent.
     * This method persists rich nested data structures to JSONB columns.
     *
     * @param txnId          Transaction ID
     * @param canonicalEvent Canonical fraud event with nested data
     * @return Updated transaction
     */
    @Transactional
    public Transaction updateWithCanonicalEvent(String txnId, CanonicalFraudEvent canonicalEvent) {
        log.info("Updating transaction with canonical event data: txnId={}", txnId);

        Transaction transaction = transactionRepository.findByTxnId(txnId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + txnId));

        // Update nested data fields (all are optional)
        if (canonicalEvent.getCustomer() != null) {
            transaction.setCustomerData(canonicalEvent.getCustomer());
            log.debug("Updated customer data for txnId={}", txnId);
        }

        if (canonicalEvent.getMerchant() != null) {
            transaction.setMerchantData(canonicalEvent.getMerchantInfo());
            log.debug("Updated merchant data for txnId={}", txnId);
        }

        if (canonicalEvent.getDevice() != null) {
            transaction.setDeviceData(canonicalEvent.getDevice());
            log.debug("Updated device data for txnId={}", txnId);
        }

        if (canonicalEvent.getLocation() != null) {
            transaction.setLocationData(canonicalEvent.getLocation());
            log.debug("Updated location data for txnId={}", txnId);
        }

        if (canonicalEvent.getBehaviorMetrics() != null) {
            transaction.setBehaviorMetrics(canonicalEvent.getBehaviorMetrics());
            log.debug("Updated behavior metrics for txnId={}", txnId);
        }

        if (canonicalEvent.getFraudSignals() != null) {
            transaction.setFraudSignals(canonicalEvent.getFraudSignals());
            log.debug("Updated fraud signals for txnId={}", txnId);
        }

        if (canonicalEvent.getMetadata() != null) {
            transaction.setMetadata(canonicalEvent.getMetadata());
            log.debug("Updated metadata for txnId={}", txnId);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction updated with canonical event data: txnId={}", txnId);

        return savedTransaction;
    }

    /**
     * Update fraud decision with agent results, risk factors, and nested canonical
     * event data.
     * This is the comprehensive update method that combines fraud decision with
     * rich nested data.
     *
     * @param txnId          Transaction ID
     * @param decision       Fraud decision (APPROVED, DECLINED, REVIEW)
     * @param score          Fraud score
     * @param agentResults   List of agent results
     * @param riskFactors    List of risk factors
     * @param canonicalEvent Canonical fraud event with nested data (optional)
     * @return Updated transaction
     */
    @Transactional
    public Transaction updateFraudDecisionWithCanonicalEvent(String txnId, String decision, java.math.BigDecimal score,
            List<AgentResult> agentResults, List<String> riskFactors,
            CanonicalFraudEvent canonicalEvent) {
        log.info("Updating fraud decision with canonical event: txnId={}, decision={}, score={}", txnId, decision,
                score);

        // First update fraud decision and details
        Transaction transaction = updateFraudDecisionWithDetails(txnId, decision, score, agentResults, riskFactors);

        // Then update nested canonical event data if provided
        if (canonicalEvent != null) {
            if (canonicalEvent.getCustomer() != null) {
                transaction.setCustomerData(canonicalEvent.getCustomer());
            }
            if (canonicalEvent.getMerchant() != null) {
                transaction.setMerchantData(canonicalEvent.getMerchantInfo());
            }
            if (canonicalEvent.getDevice() != null) {
                transaction.setDeviceData(canonicalEvent.getDevice());
            }
            if (canonicalEvent.getLocation() != null) {
                transaction.setLocationData(canonicalEvent.getLocation());
            }
            if (canonicalEvent.getBehaviorMetrics() != null) {
                transaction.setBehaviorMetrics(canonicalEvent.getBehaviorMetrics());
            }
            if (canonicalEvent.getFraudSignals() != null) {
                transaction.setFraudSignals(canonicalEvent.getFraudSignals());
            }
            if (canonicalEvent.getMetadata() != null) {
                transaction.setMetadata(canonicalEvent.getMetadata());
            }

            transaction = transactionRepository.save(transaction);
            log.info("Transaction updated with fraud decision and canonical event data: txnId={}", txnId);
        }

        return transaction;
    }

    /**
     * Publish transaction event to Kafka for fraud detection.
     *
     * @param transaction Saved transaction
     */
    private void publishTransactionEvent(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                    .transactionId(transaction.getId())
                    .txnId(transaction.getTxnId())
                    .customerId(transaction.getCustomerId())
                    .amount(transaction.getAmount())
                    .merchant(transaction.getMerchant())
                    .country(transaction.getCountry())
                    .deviceId(transaction.getDeviceId())
                    .paymentType(transaction.getPaymentType())
                    .timestamp(transaction.getTimestamp())
                    .status(transaction.getStatus())
                    .eventTime(LocalDateTime.now())
                    .build();

            kafkaProducerService.publishTransactionEvent(event);
            log.info("Transaction event published to Kafka: txnId={}", transaction.getTxnId());
        } catch (Exception e) {
            log.error("Failed to publish transaction event to Kafka: txnId={}", transaction.getTxnId(), e);
            // Don't fail the transaction if Kafka publish fails
        }
    }

    @SuppressWarnings("unused")
    private void publishTransactionEventV2(
            CanonicalFraudEvent canonicalEvent) {

        try {

            kafkaProducerService.publishTransactionEventV2(
                    canonicalEvent);

            log.info(
                    "V2 transaction event published successfully: txnId={}",
                    canonicalEvent.getTxnId());

        } catch (Exception e) {

            log.error(
                    "Error publishing V2 transaction event: txnId={}",
                    canonicalEvent.getTxnId(),
                    e);

            throw new RuntimeException(
                    "Failed to publish V2 transaction event",
                    e);
        }
    }
}

// Made with Bob
