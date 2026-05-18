package com.fraud.platform.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.CanonicalFraudEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka producer service for publishing transaction events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final KafkaTemplate<String, CanonicalFraudEvent> kafkaTemplateV2;
    @Value("${kafka.topics.fraud-transactions:fraud-transactions}")
    private String fraudTransactionsTopic;

    public void publishTransactionEventV2(
            CanonicalFraudEvent event) {

        log.info(
                "Publishing V2 transaction event: txnId={}",
                event.getTxnId());

        try {

            CompletableFuture<SendResult<String, CanonicalFraudEvent>> future = kafkaTemplateV2.send(
                    fraudTransactionsTopic,
                    event.getTxnId(),
                    event);

            future.whenComplete((result, ex) -> {

                if (ex == null) {

                    log.info(
                            "V2 transaction event published successfully: txnId={}, partition={}, offset={}",
                            event.getTxnId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());

                } else {

                    log.error(
                            "Failed to publish V2 transaction event: txnId={}",
                            event.getTxnId(),
                            ex);
                }
            });

        } catch (Exception e) {

            log.error(
                    "Error publishing V2 transaction event: txnId={}",
                    event.getTxnId(),
                    e);

            throw new RuntimeException(
                    "Failed to publish V2 transaction event",
                    e);
        }
    }

    /**
     * Publish transaction event to fraud-transactions topic.
     *
     * @param event Transaction event
     */
    public void publishTransactionEvent(TransactionEvent event) {
        log.info("Publishing transaction event to Kafka: txnId={}, topic={}",
                event.getTxnId(), fraudTransactionsTopic);

        try {
            CompletableFuture<SendResult<String, TransactionEvent>> future = kafkaTemplate.send(fraudTransactionsTopic,
                    event.getTxnId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Transaction event published successfully: txnId={}, partition={}, offset={}",
                            event.getTxnId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish transaction event: txnId={}", event.getTxnId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing transaction event to Kafka: txnId={}", event.getTxnId(), e);
            throw new RuntimeException("Failed to publish transaction event", e);
        }
    }

    /**
     * Publish transaction event synchronously (for testing).
     *
     * @param event Transaction event
     */
    public void publishTransactionEventSync(TransactionEvent event) {
        log.info("Publishing transaction event synchronously: txnId={}", event.getTxnId());

        try {
            SendResult<String, TransactionEvent> result = kafkaTemplate
                    .send(fraudTransactionsTopic, event.getTxnId(), event).get();

            log.info("Transaction event published: txnId={}, partition={}, offset={}",
                    event.getTxnId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Error publishing transaction event: txnId={}", event.getTxnId(), e);
            throw new RuntimeException("Failed to publish transaction event", e);
        }
    }
}

// Made with Bob
