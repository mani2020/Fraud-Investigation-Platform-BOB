package com.fraud.platform.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fraud.platform.entity.Transaction;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.model.TransactionRequest;
import com.fraud.platform.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for transaction operations.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Submit a new transaction for fraud detection.
     * 
     * @param request Transaction details
     * @return Created transaction
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("Received transaction request: txnId={}", request.getTxnId());

        try {
            Transaction transaction = transactionService.createTransaction(request);

            log.info("Transaction created and queued for fraud detection: txnId={}", transaction.getTxnId());

            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get transaction by transaction ID.
     * 
     * @param txnId Transaction ID
     * @return Transaction details
     */
    @GetMapping("/{txnId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String txnId) {
        log.info("Fetching transaction: txnId={}", txnId);

        return transactionService.findByTxnId(txnId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all transactions.
     * 
     * @return List of all transactions
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        log.info("Fetching all transactions");

        List<Transaction> transactions = transactionService.findAll();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions for a specific customer.
     * 
     * @param customerId Customer ID
     * @return List of customer transactions
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getCustomerTransactions(@PathVariable String customerId) {
        log.info("Fetching transactions for customer: {}", customerId);

        List<Transaction> transactions = transactionService.findByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Health check endpoint.
     * 
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transaction API is running");
    }

    @PostMapping("/v2")
    public ResponseEntity<Transaction> createTransactionV2(
            @Valid @RequestBody CanonicalFraudEvent event) {

        log.info(
                "Received v2 fraud transaction: txnId={}",
                event.getTxnId());

        /*
         * Validate required nested fields
         */
        if (event.getCustomer() == null
                || event.getTransaction() == null
                || event.getDevice() == null) {

            throw new IllegalArgumentException(
                    "Customer, transaction and device are required");
        }

        /*
         * Convert v2 nested payload
         * to existing flat TransactionRequest
         */
        TransactionRequest request = TransactionRequest.builder()
                .txnId(event.getTxnId())

                .customerId(
                        event.getCustomer()
                                .getCustomerId())

                .amount(
                        event.getTransaction()
                                .getAmount())

                .merchant(
                        event.getMerchantInfo()
                                .getMerchantName() != null
                                        ? event.getMerchantInfo()
                                                .getMerchantName()
                                        : "UNKNOWN")

                .country(
                        event.getLocation() != null
                                ? event.getLocation().getCountry()
                                : "UNKNOWN")

                .deviceId(
                        event.getDevice()
                                .getDeviceId())

                .paymentType(
                        event.getTransaction()
                                .getPaymentType())

                .timestamp(
                        event.getTimestamp() != null
                                ? event.getTimestamp()
                                : LocalDateTime.now())

                .build();

        /*
         * transaction flow
         */
        Transaction transaction = transactionService.createTransactionV2(
                request,
                event);

        log.info(
                "V2 transaction created successfully: txnId={}",
                transaction.getTxnId());

        return ResponseEntity.ok(transaction);
    }
}

// Made with Bob
