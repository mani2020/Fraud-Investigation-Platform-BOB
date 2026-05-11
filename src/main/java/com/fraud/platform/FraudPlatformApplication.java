package com.fraud.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for Fraud Investigation Platform.
 * 
 * This is a modular monolith application that provides:
 * - Real-time fraud detection using multiple agents
 * - Kafka event streaming for async processing
 * - Explainable AI for fraud decisions
 * - REST APIs for transaction processing and case management
 * - WebSocket for real-time dashboard updates
 */
@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
public class FraudPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudPlatformApplication.class, args);
    }
}

// Made with Bob
