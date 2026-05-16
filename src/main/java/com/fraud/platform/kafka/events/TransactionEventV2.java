package com.fraud.platform.kafka.events;

import com.fraud.platform.model.nested.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka event for transaction fraud analysis - Version 2 (Nested Structure).
 * 
 * <p>This is the v2 event format with nested structure for rich contextual data.
 * Published to fraud-transactions or fraud-transactions-v2 topic.</p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Nested structure for better data organization</li>
 *   <li>Rich contextual information (customer, merchant, device, location)</li>
 *   <li>Behavioral metrics and fraud signals</li>
 *   <li>Backward compatible with v1 through FraudEventDeserializer</li>
 * </ul>
 * 
 * <h2>Schema Version:</h2>
 * <p>This event uses schemaVersion "v2" to distinguish from legacy flat format (v1).</p>
 * 
 * <h2>Example JSON Payload:</h2>
 * <pre>{@code
 * {
 *   "txnId": "TXN-001",
 *   "schemaVersion": "v2",
 *   "eventTimestamp": "2025-08-07T10:15:30",
 *   "customer": {
 *     "customerId": "CUST-001",
 *     "customerName": "John Doe",
 *     "email": "john@example.com",
 *     "phoneNumber": "+1-555-0123",
 *     "riskLevel": "LOW",
 *     "accountAge": 365,
 *     "totalTransactions": 150,
 *     "avgTransactionAmount": 250.00,
 *     "lastActivityDate": "2025-08-06T15:30:00"
 *   },
 *   "transaction": {
 *     "amount": 150.00,
 *     "currency": "USD",
 *     "paymentType": "CARD",
 *     "timestamp": "2025-08-07T10:15:30",
 *     "description": "Online purchase",
 *     "status": "PENDING"
 *   },
 *   "merchant": {
 *     "merchantId": "MERCH-001",
 *     "merchantName": "Amazon.com",
 *     "merchantCategory": "E-COMMERCE",
 *     "riskLevel": "LOW",
 *     "isBlacklisted": false,
 *     "fraudRate": 0.5
 *   },
 *   "device": {
 *     "deviceId": "DEV-001",
 *     "deviceType": "MOBILE",
 *     "deviceFingerprint": "abc123xyz",
 *     "isTrusted": true,
 *     "ipAddress": "192.168.1.1",
 *     "userAgent": "Mozilla/5.0...",
 *     "vpnDetected": false,
 *     "proxyDetected": false
 *   },
 *   "location": {
 *     "country": "US",
 *     "city": "New York",
 *     "region": "NY",
 *     "latitude": 40.7128,
 *     "longitude": -74.0060,
 *     "ipAddress": "192.168.1.1",
 *     "timezone": "America/New_York"
 *   },
 *   "behaviorMetrics": {
 *     "transactionCount24h": 3,
 *     "transactionAmount24h": 450.00,
 *     "velocityScore": 0.125,
 *     "avgTransactionAmount": 250.00,
 *     "unusualTime": false,
 *     "unusualAmount": false,
 *     "unusualLocation": false
 *   },
 *   "fraudSignals": {
 *     "vpnDetected": false,
 *     "proxyDetected": false,
 *     "blacklistedMerchant": false,
 *     "blacklistedDevice": false,
 *     "blacklistedIp": false,
 *     "suspiciousPatterns": [],
 *     "riskScore": 15
 *   },
 *   "metadata": {
 *     "traceId": "trace-12345",
 *     "sessionId": "session-67890",
 *     "apiVersion": "v2",
 *     "timestamp": "2025-08-07T10:15:30",
 *     "source": "payment-gateway"
 *   }
 * }
 * }</pre>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * TransactionEventV2 event = TransactionEventV2.builder()
 *     .txnId("TXN-12345")
 *     .eventTimestamp(LocalDateTime.now())
 *     .customer(CustomerInfo.builder()
 *         .customerId("CUST-001")
 *         .customerName("John Doe")
 *         .build())
 *     .transaction(TransactionInfo.builder()
 *         .amount(new BigDecimal("1500.00"))
 *         .currency("USD")
 *         .paymentType("CREDIT_CARD")
 *         .build())
 *     .merchant(MerchantInfo.builder()
 *         .merchantName("Amazon.com")
 *         .merchantCategory("E-COMMERCE")
 *         .build())
 *     .device(DeviceInfo.builder()
 *         .deviceId("DEV-001")
 *         .deviceType("MOBILE")
 *         .isTrusted(true)
 *         .build())
 *     .location(LocationInfo.builder()
 *         .country("US")
 *         .city("New York")
 *         .build())
 *     .build();
 * }</pre>
 * 
 * @see com.fraud.platform.model.CanonicalFraudEvent
 * @see com.fraud.platform.kafka.FraudEventDeserializer
 * @see TransactionEvent for v1 (legacy flat format)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventV2 {

    /**
     * Unique transaction identifier (required).
     * This is the primary key for tracking the transaction through the fraud detection pipeline.
     */
    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    /**
     * Schema version identifier.
     * Default: "v2" (nested structure)
     * Used by FraudEventDeserializer for version-aware deserialization.
     */
    @Builder.Default
    private String schemaVersion = "v2";

    /**
     * Event creation timestamp.
     * Represents when the fraud event was created for analysis.
     */
    private LocalDateTime eventTimestamp;

    /**
     * Customer information and profile data.
     * Contains customer identity, risk level, and historical metrics.
     */
    private CustomerInfo customer;

    /**
     * Transaction details and payment information.
     * Contains amount, currency, payment type, and transaction metadata.
     */
    private TransactionInfo transaction;

    /**
     * Merchant information and risk profile.
     * Contains merchant identity, category, and fraud statistics.
     */
    private MerchantInfo merchant;

    /**
     * Device information and trust indicators.
     * Contains device identity, fingerprint, and security flags.
     */
    private DeviceInfo device;

    /**
     * Geographic location information.
     * Contains country, city, coordinates, and IP address.
     */
    private LocationInfo location;

    /**
     * Behavioral metrics and velocity indicators.
     * Contains transaction patterns, velocity scores, and anomaly flags.
     */
    private BehaviorMetrics behaviorMetrics;

    /**
     * Fraud signal indicators and risk scores.
     * Contains detected fraud patterns, blacklist flags, and risk scores.
     */
    private FraudSignals fraudSignals;

    /**
     * Request metadata and tracing information.
     * Contains trace IDs, session data, and API version information.
     */
    private MetadataInfo metadata;
}

// Made with Bob