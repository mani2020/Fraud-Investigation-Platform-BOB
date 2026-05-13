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
import org.springframework.util.StringUtils;

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
     * Valid events are retried by the listener error handler on processing failures
     * and routed to the business DLQ after retries are exhausted.
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

        validateEvent(event);

        log.info("Received transaction event: txnId={}, customerId={}, amount={}, partition={}, offset={}",
                 event.getTxnId(), event.getCustomerId(), event.getAmount(), partition, offset);

        try {
            logTransactionDetails(event);

            FraudDecision decision = fraudOrchestratorService.analyzeTransaction(event);

            log.info("Fraud analysis completed: txnId={}, decision={}, score={}",
                     event.getTxnId(), decision.getDecision(), decision.getFinalScore());

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing transaction event: txnId={}", event.getTxnId(), e);
            throw new RuntimeException("Failed to process transaction event for txnId=" + event.getTxnId(), e);
        }
    }

    private void validateEvent(TransactionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Transaction event payload is null");
        }
        if (!StringUtils.hasText(event.getTxnId())) {
            throw new IllegalArgumentException("Transaction event txnId is missing");
        }
        if (!StringUtils.hasText(event.getCustomerId())) {
            throw new IllegalArgumentException("Transaction event customerId is missing");
        }
        if (event.getAmount() == null) {
            throw new IllegalArgumentException("Transaction event amount is missing");
        }
        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException("Transaction event timestamp is missing");
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
