// package com.fraud.platform.config;

// import com.fraud.platform.entity.Transaction;
// import com.fraud.platform.model.TransactionRequest;
// import com.fraud.platform.model.nested.*;
// import com.fraud.platform.repository.TransactionRepository;
// import com.fraud.platform.service.TransactionService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.time.LocalDateTime;
// import java.util.*;

// /**
// * Data Initializer - Populates database with sample transactions on startup
// * Uses TransactionService to ensure all transactions go through Kafka fraud
// detection flow
// * Populates nested JSONB columns with rich sample data for fraud detection
// scenarios
// */
// @Configuration
// public class DataInitializer {

// private static final Logger logger =
// LoggerFactory.getLogger(DataInitializer.class);
// private static final Random random = new Random();

// @Bean
// CommandLineRunner initDatabase(TransactionRepository transactionRepository,
// TransactionService transactionService) {
// return args -> {
// // Check if data already exists
// if (transactionRepository.count() > 0) {
// logger.info("Database already contains {} transactions, skipping
// initialization",
// transactionRepository.count());
// return;
// }

// logger.info("Initializing database with sample transactions via Kafka
// flow...");

// // Create 20 sample transactions that will go through Kafka fraud detection
// String[] countries = {"USA", "UK", "India", "China", "Brazil", "Russia",
// "Nigeria", "Germany", "France", "Canada"};
// String[] cities = {"New York", "London", "Mumbai", "Beijing", "São Paulo",
// "Moscow", "Lagos", "Berlin", "Paris", "Toronto"};
// String[] merchants = {"Amazon", "Walmart", "Target", "BestBuy", "Apple
// Store", "eBay", "AliExpress", "Flipkart"};
// String[] merchantCategories = {"E-COMMERCE", "RETAIL", "ELECTRONICS",
// "MARKETPLACE", "ONLINE_STORE"};
// String[] paymentTypes = {"CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING",
// "WALLET"};

// int successCount = 0;
// for (int i = 1; i <= 20; i++) {
// try {
// // Convert double to BigDecimal for amount
// double amountValue = 100.0 + random.nextDouble() * 9900.0;
// String customerId = "CUST-" + String.format("%05d", random.nextInt(1000));
// String country = countries[random.nextInt(countries.length)];
// String city = cities[random.nextInt(cities.length)];
// String merchant = merchants[random.nextInt(merchants.length)];
// String merchantCategory =
// merchantCategories[random.nextInt(merchantCategories.length)];

// // Determine if this should be a fraudulent transaction (20% chance)
// boolean isFraudulent = random.nextDouble() < 0.2;

// TransactionRequest request = TransactionRequest.builder()
// .txnId("TXN-" + String.format("%06d", 1000 + i))
// .customerId(customerId)
// .amount(BigDecimal.valueOf(amountValue).setScale(2, RoundingMode.HALF_UP))
// .merchant(merchant)
// .country(country)
// .deviceId("DEV-" + random.nextInt(500))
// .paymentType(paymentTypes[random.nextInt(paymentTypes.length)])
// .timestamp(LocalDateTime.now().minusHours(random.nextInt(48)))
// .build();

// // Create transaction via service (publishes to Kafka)
// Transaction transaction = transactionService.createTransaction(request);

// // Populate nested JSONB data
// transaction.setCustomerData(createSampleCustomerInfo(customerId, i));
// transaction.setMerchantData(createSampleMerchantInfo(merchant,
// merchantCategory, i));
// transaction.setDeviceData(createSampleDeviceInfo(i, isFraudulent));
// transaction.setLocationData(createSampleLocationInfo(country, city, i));
// transaction.setBehaviorMetrics(createSampleBehaviorMetrics(i, isFraudulent));
// transaction.setFraudSignals(createSampleFraudSignals(isFraudulent, i));
// transaction.setMetadata(createSampleMetadata(i));

// // Save updated transaction with nested data
// transactionRepository.save(transaction);
// successCount++;

// logger.debug("Created transaction {} with nested data: txnId={},
// fraudulent={}",
// i, transaction.getTxnId(), isFraudulent);

// // Small delay to avoid overwhelming Kafka
// Thread.sleep(100);

// } catch (Exception e) {
// logger.error("Error creating sample transaction {}: {}", i, e.getMessage());
// }
// }

// logger.info("Successfully submitted {} sample transactions to Kafka for fraud
// detection", successCount);
// logger.info("Transactions will be processed asynchronously by fraud detection
// agents");
// };
// }

// /**
// * Create sample customer information with realistic data
// */
// private CustomerInfo createSampleCustomerInfo(String customerId, int index) {
// return CustomerInfo.builder()
// .customerId(customerId)
// .customerName("Customer " + customerId)
// .email(customerId.toLowerCase() + "@example.com")
// .phone("+1-555-" + String.format("%04d", 1000 + index))
// .accountAge(30 + random.nextInt(1800)) // 30 to 1830 days
// .riskLevel(random.nextDouble() < 0.8 ? "LOW" : "MEDIUM")
// .totalTransactions((long) (random.nextInt(500) + 10))
// .avgTransactionAmount(BigDecimal.valueOf(500 + random.nextDouble() *
// 2000).setScale(2, RoundingMode.HALF_UP))
// .lastActivityDate(LocalDateTime.now().minusDays(random.nextInt(30)))
// .build();
// }

// /**
// * Create sample merchant information
// */
// private MerchantInfo createSampleMerchantInfo(String merchantName, String
// category, int index) {
// return MerchantInfo.builder()
// .merchantId("MERCH-" + String.format("%05d", index))
// .merchantName(merchantName)
// .merchantCategory(category)
// .country(random.nextBoolean() ? "USA" : "UK")
// .fraudRate(BigDecimal.valueOf(random.nextDouble() * 0.1).setScale(3,
// RoundingMode.HALF_UP))
// .riskLevel(random.nextDouble() < 0.9 ? "LOW" : "MEDIUM")
// .isBlacklisted(random.nextDouble() < 0.05) // 5% blacklisted
// .build();
// }

// /**
// * Create sample device information with fingerprinting
// */
// private DeviceInfo createSampleDeviceInfo(int index, boolean isFraudulent) {
// return DeviceInfo.builder()
// .deviceId("DEV-" + String.format("%05d", index))
// .deviceType(random.nextBoolean() ? "MOBILE" : "DESKTOP")
// .deviceFingerprint("FP-" + UUID.randomUUID().toString().substring(0, 16))
// .isTrusted(!isFraudulent || random.nextBoolean())
// .ipAddress("192.168." + random.nextInt(255) + "." + random.nextInt(255))
// .vpnDetected(isFraudulent && random.nextBoolean()) // VPN more likely for
// fraud
// .proxyDetected(isFraudulent && random.nextBoolean())
// .userAgent((random.nextBoolean() ? "Chrome" : "Safari") + "/" + (90 +
// random.nextInt(20)) + ".0")
// .build();
// }

// /**
// * Create sample location information
// */
// private LocationInfo createSampleLocationInfo(String country, String city,
// int index) {
// // Convert country name to 2-letter code
// String countryCode = switch (country) {
// case "USA" -> "US";
// case "UK" -> "GB";
// case "India" -> "IN";
// case "China" -> "CN";
// case "Brazil" -> "BR";
// case "Russia" -> "RU";
// case "Nigeria" -> "NG";
// case "Germany" -> "DE";
// case "France" -> "FR";
// case "Canada" -> "CA";
// default -> "US";
// };

// return LocationInfo.builder()
// .country(countryCode)
// .city(city)
// .region(city + " Region")
// .latitude(BigDecimal.valueOf(-90 + random.nextDouble() * 180).setScale(6,
// RoundingMode.HALF_UP))
// .longitude(BigDecimal.valueOf(-180 + random.nextDouble() * 360).setScale(6,
// RoundingMode.HALF_UP))
// .ipAddress("192.168." + random.nextInt(255) + "." + random.nextInt(255))
// .timezone("UTC" + (random.nextBoolean() ? "+" : "-") + random.nextInt(12))
// .build();
// }

// /**
// * Create sample behavior metrics with velocity data
// */
// private BehaviorMetrics createSampleBehaviorMetrics(int index, boolean
// isFraudulent) {
// return BehaviorMetrics.builder()
// .transactionCount24h(isFraudulent ? random.nextInt(50) + 20 :
// random.nextInt(15))
// .totalAmount24h(BigDecimal.valueOf(random.nextDouble() * 15000).setScale(2,
// RoundingMode.HALF_UP))
// .velocityScore(BigDecimal.valueOf(isFraudulent ? 70 + random.nextDouble() *
// 30 : random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP))
// .unusualTime(isFraudulent && random.nextBoolean())
// .unusualAmount(isFraudulent && random.nextBoolean())
// .unusualLocation(isFraudulent && random.nextBoolean())
// .avgTransactionAmount(BigDecimal.valueOf(500 + random.nextDouble() *
// 2000).setScale(2, RoundingMode.HALF_UP))
// .maxTransactionAmount(BigDecimal.valueOf(1000 + random.nextDouble() *
// 9000).setScale(2, RoundingMode.HALF_UP))
// .build();
// }

// /**
// * Create sample fraud signals based on transaction type
// */
// private FraudSignals createSampleFraudSignals(boolean isFraudulent, int
// index) {
// List<String> patterns = new ArrayList<>();
// if (isFraudulent) {
// if (random.nextBoolean()) patterns.add("HIGH_VELOCITY");
// if (random.nextBoolean()) patterns.add("UNUSUAL_LOCATION");
// if (random.nextBoolean()) patterns.add("NEW_DEVICE");
// if (random.nextBoolean()) patterns.add("VPN_DETECTED");
// if (random.nextBoolean()) patterns.add("AMOUNT_ANOMALY");
// }

// return FraudSignals.builder()
// .vpnDetected(isFraudulent && random.nextBoolean())
// .proxyDetected(isFraudulent && random.nextBoolean())
// .deviceMismatch(isFraudulent && random.nextBoolean())
// .locationMismatch(isFraudulent && random.nextBoolean())
// .blacklistedMerchant(isFraudulent && random.nextDouble() < 0.2)
// .blacklistedDevice(isFraudulent && random.nextDouble() < 0.1)
// .blacklistedIp(isFraudulent && random.nextDouble() < 0.15)
// .riskScore(isFraudulent ? 70 + random.nextInt(31) : random.nextInt(31))
// .suspiciousPatterns(patterns)
// .build();
// }

// /**
// * Create sample metadata information
// */
// private MetadataInfo createSampleMetadata(int index) {
// return MetadataInfo.builder()
// .traceId("TRACE-" + UUID.randomUUID().toString())
// .sessionId("SESS-" + UUID.randomUUID().toString().substring(0, 16))
// .userAgent((random.nextBoolean() ? "Chrome" : "Safari") + "/" + (90 +
// random.nextInt(20)) + ".0")
// .referrer(random.nextBoolean() ? "https://example.com" :
// "https://app.example.com")
// .apiVersion("v2")
// .clientVersion("1.0." + random.nextInt(10))
// .timestamp(LocalDateTime.now())
// .build();
// }
// }

// // Made with Bob