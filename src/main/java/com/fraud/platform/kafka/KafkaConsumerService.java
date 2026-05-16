package com.fraud.platform.kafka;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.mapper.TransactionEventMapper;
import com.fraud.platform.model.CanonicalFraudEvent;
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
 *
 * <p>Supports both v1 (flat) and v2 (nested) event formats with seamless backward compatibility.
 * Uses FraudEventDeserializer for version-aware deserialization and TransactionEventMapper
 * for transformation to CanonicalFraudEvent.</p>
 *
 * <h2>Supported Formats:</h2>
 * <ul>
 *   <li><b>v1 (Legacy)</b>: Flat TransactionEvent structure</li>
 *   <li><b>v2 (Current)</b>: Nested CanonicalFraudEvent structure</li>
 * </ul>
 *
 * <h2>Version Detection:</h2>
 * <p>The FraudEventDeserializer automatically detects the schema version from the
 * "schemaVersion" field in the JSON payload and deserializes to the appropriate format.</p>
 *
 * @see FraudEventDeserializer
 * @see TransactionEventMapper
 * @see CanonicalFraudEvent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final FraudOrchestratorService fraudOrchestratorService;
    private final TransactionEventMapper transactionEventMapper;

    /**
     * Consume transaction events from fraud-transactions topic (v1 - Legacy Format).
     *
     * <p>This listener handles legacy flat TransactionEvent format for backward compatibility.
     * Events are transformed to CanonicalFraudEvent before processing.</p>
     *
     * <p>Valid events are retried by the listener error handler on processing failures
     * and routed to the business DLQ after retries are exhausted.</p>
     *
     * @param event Legacy transaction event (v1 format)
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

        log.info("Received v1 transaction event: txnId={}, customerId={}, amount={}, partition={}, offset={}",
                 event.getTxnId(), event.getCustomerId(), event.getAmount(), partition, offset);

        try {
            logTransactionDetails(event);

            // Transform v1 to canonical format
            CanonicalFraudEvent canonicalEvent = transactionEventMapper.fromLegacy(event);
            log.debug("Transformed v1 event to canonical format: txnId={}, schemaVersion={}",
                     canonicalEvent.getTxnId(), canonicalEvent.getSchemaVersion());

            // Process using canonical format (preparing for Phase 5)
            FraudDecision decision = fraudOrchestratorService.analyzeTransaction(canonicalEvent);

            log.info("Fraud analysis completed: txnId={}, decision={}, score={}",
                     event.getTxnId(), decision.getDecision(), decision.getFinalScore());

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing v1 transaction event: txnId={}", event.getTxnId(), e);
            throw new RuntimeException("Failed to process v1 transaction event for txnId=" + event.getTxnId(), e);
        }
    }

    /**
     * Consume transaction events from fraud-transactions topic (v2 - Nested Format).
     *
     * <p>This listener handles the new nested CanonicalFraudEvent format with rich contextual data.
     * This is the preferred format for new integrations.</p>
     *
     * <p>Version detection is handled automatically by FraudEventDeserializer based on the
     * "schemaVersion" field in the JSON payload.</p>
     *
     * <p>Valid events are retried by the listener error handler on processing failures
     * and routed to the business DLQ after retries are exhausted.</p>
     *
     * @param event Canonical fraud event (v2 format)
     * @param partition Kafka partition
     * @param offset Kafka offset
     * @param acknowledgment Manual acknowledgment
     */
    @KafkaListener(
        topics = "${kafka.topics.fraud-transactions:fraud-transactions}",
        groupId = "${spring.kafka.consumer.group-id}-v2",
        containerFactory = "canonicalKafkaListenerContainerFactory"
    )
    public void consumeTransactionV2(
            @Payload CanonicalFraudEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        validateCanonicalEvent(event);

        log.info("Received v2 canonical event: txnId={}, customerId={}, amount={}, schemaVersion={}, partition={}, offset={}",
                 event.getTxnId(), event.getCustomerId(), event.getAmount(),
                 event.getSchemaVersion(), partition, offset);

        try {
            logCanonicalEventDetails(event);

            // Enrich event with additional data
            CanonicalFraudEvent enrichedEvent = transactionEventMapper.enrich(event);
            log.debug("Enriched v2 event: txnId={}, riskScore={}",
                     enrichedEvent.getTxnId(), enrichedEvent.getRiskScore());

            // TODO Phase 5: Update orchestrator to accept CanonicalFraudEvent
            // For now, convert to legacy format for orchestrator compatibility
            TransactionEvent legacyEvent = transactionEventMapper.toLegacy(enrichedEvent);
            FraudDecision decision = fraudOrchestratorService.analyzeTransaction(enrichedEvent);

            log.info("Fraud analysis completed: txnId={}, decision={}, score={}",
                     event.getTxnId(), decision.getDecision(), decision.getFinalScore());

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing v2 canonical event: txnId={}", event.getTxnId(), e);
            throw new RuntimeException("Failed to process v2 canonical event for txnId=" + event.getTxnId(), e);
        }
    }

    /**
     * Validate v1 transaction event.
     *
     * @param event Transaction event to validate
     * @throws IllegalArgumentException if validation fails
     */
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
     * Validate v2 canonical fraud event.
     *
     * @param event Canonical fraud event to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCanonicalEvent(CanonicalFraudEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Canonical fraud event payload is null");
        }
        if (!StringUtils.hasText(event.getTxnId())) {
            throw new IllegalArgumentException("Canonical fraud event txnId is missing");
        }
        if (event.getCustomerId() == null) {
            throw new IllegalArgumentException("Canonical fraud event customerId is missing (customer object may be null)");
        }
        if (event.getAmount() == null) {
            throw new IllegalArgumentException("Canonical fraud event amount is missing (transaction object may be null)");
        }
        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException("Canonical fraud event timestamp is missing (transaction object may be null)");
        }
    }

    /**
     * Log v1 transaction event details for debugging.
     *
     * @param event Transaction event
     */
    private void logTransactionDetails(TransactionEvent event) {
        log.debug("V1 Transaction Details - " +
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

    /**
     * Log v2 canonical event details for debugging.
     *
     * @param event Canonical fraud event
     */
    private void logCanonicalEventDetails(CanonicalFraudEvent event) {
        log.debug("V2 Canonical Event Details - " +
                  "txnId: {}, " +
                  "schemaVersion: {}, " +
                  "customerId: {}, " +
                  "amount: {}, " +
                  "merchant: {}, " +
                  "country: {}, " +
                  "deviceId: {}, " +
                  "paymentType: {}, " +
                  "timestamp: {}, " +
                  "riskLevel: {}, " +
                  "deviceTrusted: {}, " +
                  "vpnDetected: {}",
                  event.getTxnId(),
                  event.getSchemaVersion(),
                  event.getCustomerId(),
                  event.getAmount(),
                  event.getMerchant(),
                  event.getCountry(),
                  event.getDeviceId(),
                  event.getPaymentType(),
                  event.getTimestamp(),
                  event.getCustomerRiskLevel(),
                  event.isDeviceTrusted(),
                  event.isVpnDetected());
    }
}

// Made with Bob
