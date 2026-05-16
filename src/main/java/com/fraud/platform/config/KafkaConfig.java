package com.fraud.platform.config;

import com.fraud.platform.kafka.FraudEventDeserializer;
import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.model.CanonicalFraudEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for fraud detection platform.
 *
 * <p>Supports both v1 (flat TransactionEvent) and v2 (nested CanonicalFraudEvent) formats
 * with version-aware deserialization using FraudEventDeserializer.</p>
 *
 * <h2>Consumer Factories:</h2>
 * <ul>
 *   <li><b>consumerFactory</b>: For v1 legacy TransactionEvent format</li>
 *   <li><b>canonicalConsumerFactory</b>: For v2 CanonicalFraudEvent format with FraudEventDeserializer</li>
 * </ul>
 *
 * <h2>Listener Container Factories:</h2>
 * <ul>
 *   <li><b>kafkaListenerContainerFactory</b>: For v1 consumers</li>
 *   <li><b>canonicalKafkaListenerContainerFactory</b>: For v2 consumers</li>
 * </ul>
 *
 * @see FraudEventDeserializer
 * @see CanonicalFraudEvent
 * @see TransactionEvent
 */
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.fraud-transactions}")
    private String fraudTransactionsTopic;

    @Value("${kafka.topics.fraud-results}")
    private String fraudDecisionsTopic;

    @Value("${kafka.topics.fraud-transactions-dlq}")
    private String fraudTransactionsDlqTopic;

    @Value("${kafka.topics.fraud-invalid-events}")
    private String fraudInvalidEventsTopic;

    @Value("${kafka.topics.partitions:3}")
    private int partitions;

    @Value("${kafka.topics.replication-factor:1}")
    private int replicationFactor;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.listener.retry.fixed-backoff-ms:1000}")
    private long retryBackoffMs;

    @Value("${spring.kafka.listener.retry.max-attempts:3}")
    private long maxAttempts;

    /**
     * Create fraud-transactions topic.
     * This topic receives all incoming payment transactions for fraud analysis.
     */
    @Bean
    public NewTopic fraudTransactionsTopic() {
        return TopicBuilder.name(fraudTransactionsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    /**
     * Create fraud-decisions topic.
     * This topic publishes fraud detection decisions and scores.
     */
    @Bean
    public NewTopic fraudDecisionsTopic() {
        return TopicBuilder.name(fraudDecisionsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    /**
     * Create DLQ topic for valid business events that fail repeatedly during processing.
     */
    @Bean
    public NewTopic fraudTransactionsDlqTopic() {
        return TopicBuilder.name(fraudTransactionsDlqTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    /**
     * Create topic for malformed or invalid events rejected before business processing.
     */
    @Bean
    public NewTopic fraudInvalidEventsTopic() {
        return TopicBuilder.name(fraudInvalidEventsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    /**
     * Consumer factory for v1 legacy TransactionEvent format.
     *
     * <p>Uses ErrorHandlingDeserializer so deserialization failures are surfaced
     * to the container error handler and can be redirected to the invalid events topic.</p>
     */
    @Bean
    public ConsumerFactory<String, TransactionEvent> consumerFactory() {
        JsonDeserializer<TransactionEvent> jsonDeserializer = new JsonDeserializer<>(TransactionEvent.class);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<TransactionEvent> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);
        ErrorHandlingDeserializer<String> keyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(
                props,
                keyDeserializer,
                valueDeserializer
        );
    }

    /**
     * Consumer factory for v2 CanonicalFraudEvent format with version-aware deserialization.
     *
     * <p>Uses FraudEventDeserializer which automatically detects schema version and
     * deserializes to CanonicalFraudEvent. Supports both v1 and v2 payloads on the same topic.</p>
     */
    @Bean
    public ConsumerFactory<String, CanonicalFraudEvent> canonicalConsumerFactory() {
        FraudEventDeserializer fraudEventDeserializer = new FraudEventDeserializer();
        
        ErrorHandlingDeserializer<CanonicalFraudEvent> valueDeserializer =
                new ErrorHandlingDeserializer<>(fraudEventDeserializer);
        ErrorHandlingDeserializer<String> keyDeserializer =
                new ErrorHandlingDeserializer<>(new StringDeserializer());

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-v2");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(
                props,
                keyDeserializer,
                valueDeserializer
        );
    }

    /**
     * Listener container factory for v1 legacy TransactionEvent format.
     *
     * <p>Configured with retry and topic-based dead letter routing:</p>
     * <ul>
     *   <li>business processing failures → fraud-transactions-dlq</li>
     *   <li>deserialization/invalid payload failures → fraud-invalid-events</li>
     * </ul>
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, TransactionEvent> consumerFactory,
            CommonErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, TransactionEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.setRecordFilterStrategy((RecordFilterStrategy<String, TransactionEvent>) record -> false);
        return factory;
    }

    /**
     * Listener container factory for v2 CanonicalFraudEvent format.
     *
     * <p>Configured with retry and topic-based dead letter routing:</p>
     * <ul>
     *   <li>business processing failures → fraud-transactions-dlq</li>
     *   <li>deserialization/invalid payload failures → fraud-invalid-events</li>
     * </ul>
     *
     * <p>Uses FraudEventDeserializer for version-aware deserialization supporting both
     * v1 and v2 payloads on the same topic.</p>
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CanonicalFraudEvent> canonicalKafkaListenerContainerFactory(
            ConsumerFactory<String, CanonicalFraudEvent> canonicalConsumerFactory,
            CommonErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, CanonicalFraudEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(canonicalConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.setRecordFilterStrategy((RecordFilterStrategy<String, CanonicalFraudEvent>) record -> false);
        return factory;
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> isInvalidPayloadException(exception)
                        ? new TopicPartition(fraudInvalidEventsTopic, record.partition())
                        : new TopicPartition(fraudTransactionsDlqTopic, record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(retryBackoffMs, Math.max(0, maxAttempts - 1))
        );

        errorHandler.addNotRetryableExceptions(
                SerializationException.class,
                org.springframework.kafka.support.serializer.DeserializationException.class,
                IllegalArgumentException.class
        );

        return errorHandler;
    }

    private boolean isInvalidPayloadException(Exception exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SerializationException
                    || current instanceof org.springframework.kafka.support.serializer.DeserializationException
                    || current instanceof IllegalArgumentException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

// Made with Bob