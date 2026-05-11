package com.fraud.platform.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration for fraud detection platform.
 * Creates required topics and configures Kafka settings.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.fraud-transactions}")
    private String fraudTransactionsTopic;

    @Value("${kafka.topics.fraud-results}")
    private String fraudDecisionsTopic;

    /**
     * Create fraud-transactions topic.
     * This topic receives all incoming payment transactions for fraud analysis.
     */
    @Bean
    public NewTopic fraudTransactionsTopic() {
        return TopicBuilder.name(fraudTransactionsTopic)
                .partitions(3)  // 3 partitions for parallel processing
                .replicas(1)    // 1 replica for dev environment
                .compact()      // Enable log compaction
                .build();
    }

    /**
     * Create fraud-decisions topic.
     * This topic publishes fraud detection decisions and scores.
     */
    @Bean
    public NewTopic fraudDecisionsTopic() {
        return TopicBuilder.name(fraudDecisionsTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }
}

// Made with Bob