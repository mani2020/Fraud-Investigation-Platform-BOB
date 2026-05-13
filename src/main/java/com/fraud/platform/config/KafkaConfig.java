package com.fraud.platform.config;

import com.fraud.platform.kafka.events.TransactionEvent;
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
 * Creates required topics and configures Kafka settings.
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
     * Consumer factory with ErrorHandlingDeserializer so deserialization failures are surfaced
     * to the container error handler and can be redirected to the invalid events topic.
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
     * Listener container factory with retry and topic-based dead letter routing:
     * - business processing failures -> fraud-transactions-dlq
     * - deserialization/invalid payload failures -> fraud-invalid-events
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