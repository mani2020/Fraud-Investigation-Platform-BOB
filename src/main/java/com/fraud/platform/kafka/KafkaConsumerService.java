package com.fraud.platform.kafka;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.FraudDecision;
import com.fraud.platform.orchestrator.FraudOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service for fraud orchestration.
 * Consumes from fraud-transactions topic and triggers fraud detection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final FraudOrchestratorService fraudOrchestratorService;

    /**
     * Consume transaction events from fraud-transactions topic.
     * This is the entry point for fraud detection orchestration.
     *
     * @param event Transaction event
     * @param partition Kafka partition
     * @param offset Kafka offset
     * @param acknowledgment Manual acknowledgment
     */
    @KafkaListener(
        topics = "${kafka.topics.fraud-transactions:fraud-transactions}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionEvent(
            @Payload TransactionEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received transaction event: txnId={}, customerId={}, amount={}, partition={}, offset={}",
                 event.getTxnId(), event.getCustomerId(), event.getAmount(), partition, offset);

        try {
            // Log transaction details
            logTransactionDetails(event);
            
            // Trigger fraud detection orchestration
            FraudDecision decision = fraudOrchestratorService.analyzeTransaction(event);
            
            log.info("Fraud analysis completed: txnId={}, decision={}, score={}",
                     event.getTxnId(), decision.getDecision(), decision.getFinalScore());
            
            // Manually acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing transaction event: txnId={}", event.getTxnId(), e);
            // Don't acknowledge - message will be reprocessed
            throw new RuntimeException("Failed to process transaction event", e);
        }
    }

    /**
     * Log transaction event details for debugging.
     *
     * @param event Transaction event
     */
    private void logTransactionDetails(TransactionEvent event) {
        log.debug("Transaction Details - " +
                  "txnId: {}, " +
                  "customerId: {}, " +
                  "amount: {}, " +
                  "merchant: {}, " +
                  "country: {}, " +
                  "deviceId: {}, " +
                  "paymentType: {}, " +
                  "timestamp: {}",
                  event.getTxnId(),
                  event.getCustomerId(),
                  event.getAmount(),
                  event.getMerchant(),
                  event.getCountry(),
                  event.getDeviceId(),
                  event.getPaymentType(),
                  event.getTimestamp());
    }
}

// Made with Bob
