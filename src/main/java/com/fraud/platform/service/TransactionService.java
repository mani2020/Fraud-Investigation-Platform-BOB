package com.fraud.platform.service;

import com.fraud.platform.entity.Transaction;
import com.fraud.platform.kafka.KafkaProducerService;
import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.TransactionRequest;
import com.fraud.platform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing payment transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;

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

        // Publish to Kafka for fraud detection
        publishTransactionEvent(savedTransaction);

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
}

// Made with Bob
