package com.fraud.platform.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fraud.platform.kafka.FraudEventDeserializer;
import com.fraud.platform.mapper.TransactionEventMapper;
import com.fraud.platform.model.nested.BehaviorMetrics;
import com.fraud.platform.model.nested.CustomerInfo;
import com.fraud.platform.model.nested.DeviceInfo;
import com.fraud.platform.model.nested.FraudSignals;
import com.fraud.platform.model.nested.LocationInfo;
import com.fraud.platform.model.nested.MerchantInfo;
import com.fraud.platform.model.nested.MetadataInfo;
import com.fraud.platform.model.nested.TransactionInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canonical internal fraud event model used by all fraud detection agents.
 * 
 * <p>
 * This model serves as the unified internal representation for fraud analysis,
 * supporting both v1 (flat) and v2 (nested) payload formats while maintaining
 * backward compatibility.
 * </p>
 * 
 * <h2>Schema Versions:</h2>
 * <ul>
 * <li><b>v1</b>: Legacy flat structure (TransactionEvent)</li>
 * <li><b>v2</b>: Nested structure with rich contextual data (default)</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <h3>1. Creating a v2 Canonical Event:</h3>
 * 
 * <pre>{@code
 * CanonicalFraudEvent event = CanonicalFraudEvent.builder()
 *         .txnId("TXN-12345")
 *         .eventTimestamp(LocalDateTime.now())
 *         .customer(CustomerInfo.builder()
 *                 .customerId("CUST-001")
 *                 .customerName("John Doe")
 *                 .build())
 *         .transaction(TransactionInfo.builder()
 *                 .amount(new BigDecimal("1500.00"))
 *                 .paymentType("CREDIT_CARD")
 *                 .build())
 *         .build();
 * }</pre>
 * 
 * <h3>2. Using Backward Compatibility Helpers:</h3>
 * 
 * <pre>{@code
 * // Access nested fields using flat accessors
 * String customerId = event.getCustomerId(); // Returns customer.customerId
 * BigDecimal amount = event.getAmount(); // Returns transaction.amount
 * String merchant = event.getMerchant(); // Returns merchant.merchantName
 * }</pre>
 * 
 * <h3>3. Null-Safe Access:</h3>
 * 
 * <pre>{@code
 * // All helper methods handle null nested objects gracefully
 * CanonicalFraudEvent event = new CanonicalFraudEvent();
 * String customerId = event.getCustomerId(); // Returns null, no NPE
 * }</pre>
 * 
 * @see TransactionEventMapper for transformation between formats
 * @see FraudEventDeserializer for version-aware deserialization
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalFraudEvent {

    /**
     * Unique transaction identifier (required).
     */
    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    /**
     * Schema version identifier.
     * Default: "v2" (nested structure)
     * Legacy: "v1" (flat structure)
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
    @Valid
    private CustomerInfo customer;

    /**
     * Transaction details and payment information.
     * Contains amount, currency, payment type, and transaction metadata.
     */
    @Valid
    private TransactionInfo transaction;

    /**
     * Merchant information and risk profile.
     * Contains merchant identity, category, and fraud statistics.
     */
    @Valid
    private MerchantInfo merchant;

    /**
     * Device information and trust indicators.
     * Contains device identity, fingerprint, and security flags.
     */
    @Valid
    private DeviceInfo device;

    /**
     * Geographic location information.
     * Contains country, city, coordinates, and IP address.
     */
    @Valid
    private LocationInfo location;

    /**
     * Behavioral metrics and velocity indicators.
     * Contains transaction patterns, velocity scores, and anomaly flags.
     */
    @Valid
    private BehaviorMetrics behaviorMetrics;

    /**
     * Fraud signal indicators and risk scores.
     * Contains detected fraud patterns, blacklist flags, and risk scores.
     */
    @Valid
    private FraudSignals fraudSignals;

    /**
     * Request metadata and tracing information.
     * Contains trace IDs, session data, and API version information.
     */
    @Valid
    private MetadataInfo metadata;

    // ==================== Backward Compatibility Helper Methods
    // ====================

    /**
     * Get customer ID from nested customer object.
     *
     * @return customer ID or null if customer is null
     */
    @JsonIgnore
    public String getCustomerId() {
        return customer != null
                ? customer.getCustomerId()
                : null;
    }

    /**
     * Get transaction amount from nested transaction object.
     *
     * @return transaction amount or null if transaction is null
     */
    @JsonIgnore
    public BigDecimal getAmount() {
        return transaction != null
                ? transaction.getAmount()
                : null;
    }

    /**
     * Get merchant name from nested merchant object.
     *
     * @return merchant name or null if merchant is null
     */
    @JsonIgnore
    public String getMerchantName() {
        return merchant != null
                ? merchant.getMerchantName()
                : null;
    }

    /**
     * Get merchant info object.
     *
     * @return merchant info or null if merchant is null
     */
    @JsonIgnore
    public MerchantInfo getMerchantInfo() {
        return merchant;
    }

    /**
     * Get country from nested location object.
     *
     * @return country code or null if location is null
     */
    @JsonIgnore
    public String getCountry() {
        return location != null
                ? location.getCountry()
                : null;
    }

    /**
     * Get device ID from nested device object.
     *
     * @return device ID or null if device is null
     */
    @JsonIgnore
    public String getDeviceId() {
        return device != null
                ? device.getDeviceId()
                : null;
    }

    /**
     * Get payment type from nested transaction object.
     *
     * @return payment type or null if transaction is null
     */
    @JsonIgnore
    public String getPaymentType() {
        return transaction != null
                ? transaction.getPaymentType()
                : null;
    }

    /**
     * Get transaction timestamp from nested transaction object.
     *
     * @return transaction timestamp or null if transaction is null
     */
    @JsonIgnore
    public LocalDateTime getTimestamp() {
        return transaction != null
                ? transaction.getTimestamp()
                : eventTimestamp;
    }

    // ==================== Additional Helper Methods ====================

    /**
     * Check if this is a v1 (legacy) event.
     *
     * @return true if schema version is v1
     */
    @JsonIgnore
    public boolean isLegacyEvent() {
        return "v1".equals(schemaVersion);
    }

    /**
     * Check if this is a v2 (nested) event.
     *
     * @return true if schema version is v2
     */
    @JsonIgnore
    public boolean isNestedEvent() {
        return "v2".equals(schemaVersion);
    }

    /**
     * Get customer risk level from nested customer object.
     *
     * @return customer risk level or null if customer is null
     */
    @JsonIgnore
    public String getCustomerRiskLevel() {
        return customer != null
                ? customer.getRiskLevel()
                : null;
    }

    /**
     * Get merchant risk level from nested merchant object.
     *
     * @return merchant risk level or null if merchant is null
     */
    @JsonIgnore
    public String getMerchantRiskLevel() {
        return merchant != null
                ? merchant.getRiskLevel()
                : null;
    }

    /**
     * Check if device is trusted from nested device object.
     *
     * @return true if device is trusted
     */
    @JsonIgnore
    public boolean isDeviceTrusted() {
        return device != null
                && Boolean.TRUE.equals(device.getIsTrusted());
    }

    /**
     * Check if VPN was detected from nested fraud signals.
     *
     * @return true if VPN detected
     */
    @JsonIgnore
    public boolean isVpnDetected() {
        return fraudSignals != null
                && Boolean.TRUE.equals(fraudSignals.getVpnDetected());
    }

    /**
     * Get aggregated risk score from nested fraud signals.
     *
     * @return risk score or null
     */
    @JsonIgnore
    public Integer getRiskScore() {
        return fraudSignals != null
                ? fraudSignals.getRiskScore()
                : null;
    }

    /**
     * Get velocity score from nested behavior metrics.
     *
     * @return velocity score or null
     */
    @JsonIgnore
    public BigDecimal getVelocityScore() {
        return behaviorMetrics != null
                ? behaviorMetrics.getVelocityScore()
                : null;
    }

    /**
     * Get IP address from nested device or location object.
     *
     * @return IP address or null
     */
    @JsonIgnore
    public String getIpAddress() {

        if (device != null
                && device.getIpAddress() != null) {
            return device.getIpAddress();
        }

        return location != null
                ? location.getIpAddress()
                : null;
    }

    /**
     * Get trace ID from nested metadata object.
     *
     * @return trace ID or null
     */
    @JsonIgnore
    public String getTraceId() {
        return metadata != null
                ? metadata.getTraceId()
                : null;
    }

    /**
     * Check if any critical fraud signals are present.
     *
     * @return true if blacklisted merchant, device, or IP detected
     */
    @JsonIgnore
    public boolean hasCriticalFraudSignals() {

        if (fraudSignals == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                fraudSignals.getBlacklistedMerchant())
                || Boolean.TRUE.equals(
                        fraudSignals.getBlacklistedDevice())
                || Boolean.TRUE.equals(
                        fraudSignals.getBlacklistedIp());
    }

    /**
     * Check if behavioral anomalies are present.
     *
     * @return true if unusual time, amount, or location detected
     */
    @JsonIgnore
    public boolean hasBehavioralAnomalies() {

        if (behaviorMetrics == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                behaviorMetrics.getUnusualTime())
                || Boolean.TRUE.equals(
                        behaviorMetrics.getUnusualAmount())
                || Boolean.TRUE.equals(
                        behaviorMetrics.getUnusualLocation());
    }
}

// Made with Bob