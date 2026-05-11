package com.fraud.platform.config;

import com.fraud.platform.model.TransactionRequest;
import com.fraud.platform.repository.TransactionRepository;
import com.fraud.platform.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Data Initializer - Populates database with sample transactions on startup
 * Uses TransactionService to ensure all transactions go through Kafka fraud detection flow
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final Random random = new Random();

    @Bean
    CommandLineRunner initDatabase(TransactionRepository transactionRepository, TransactionService transactionService) {
        return args -> {
            // Check if data already exists
            if (transactionRepository.count() > 0) {
                logger.info("Database already contains {} transactions, skipping initialization",
                    transactionRepository.count());
                return;
            }

            logger.info("Initializing database with sample transactions via Kafka flow...");
            
            // Create 20 sample transactions that will go through Kafka fraud detection
            String[] countries = {"USA", "UK", "India", "China", "Brazil", "Russia", "Nigeria", "Germany", "France", "Canada"};
            String[] merchants = {"Amazon", "Walmart", "Target", "BestBuy", "Apple Store", "eBay", "AliExpress", "Flipkart"};
            String[] paymentTypes = {"CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING", "WALLET"};
            
            int successCount = 0;
            for (int i = 1; i <= 20; i++) {
                try {
                    // Convert double to BigDecimal for amount
                    double amountValue = 100.0 + random.nextDouble() * 9900.0;
                    
                    TransactionRequest request = TransactionRequest.builder()
                        .txnId("TXN-" + String.format("%06d", 1000 + i))
                        .customerId("CUST-" + String.format("%05d", random.nextInt(1000)))
                        .amount(BigDecimal.valueOf(amountValue).setScale(2, RoundingMode.HALF_UP))
                        .merchant(merchants[random.nextInt(merchants.length)])
                        .country(countries[random.nextInt(countries.length)])
                        .deviceId("DEV-" + random.nextInt(500))
                        .paymentType(paymentTypes[random.nextInt(paymentTypes.length)])
                        .timestamp(LocalDateTime.now().minusHours(random.nextInt(48)))
                        .build();
                    
                    // Use TransactionService which will publish to Kafka
                    transactionService.createTransaction(request);
                    successCount++;
                    
                    // Small delay to avoid overwhelming Kafka
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    logger.error("Error creating sample transaction {}: {}", i, e.getMessage());
                }
            }
            
            logger.info("Successfully submitted {} sample transactions to Kafka for fraud detection", successCount);
            logger.info("Transactions will be processed asynchronously by fraud detection agents");
        };
    }
}

// Made with Bob