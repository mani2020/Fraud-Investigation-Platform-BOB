package com.fraud.platform.mapper;

import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.kafka.events.TransactionEventV2;
import com.fraud.platform.model.CanonicalFraudEvent;
import com.fraud.platform.model.TransactionRequest;
import com.fraud.platform.model.nested.*;
import com.fraud.platform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for transforming between different transaction payload formats.
 *
 * <p>
 * This mapper handles bidirectional transformation between:
 * </p>
 * <ul>
 * <li>Legacy flat TransactionEvent (v1) ↔ CanonicalFraudEvent</li>
 * <li>Nested TransactionEventV2 (v2) ↔ CanonicalFraudEvent</li>
 * <li>Flat TransactionRequest (API) → CanonicalFraudEvent</li>
 * <li>CanonicalFraudEvent → Legacy TransactionEvent (backward
 * compatibility)</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <h3>1. Transform Legacy Event to Canonical:</h3>
 * 
 * <pre>{@code
 * &#64;Autowired
 * private TransactionEventMapper mapper;
 * 
 * TransactionEvent legacyEvent = // ... from Kafka
 * CanonicalFraudEvent canonical = mapper.fromLegacy(legacyEvent);
 * // canonical now has v1 schema with flat data mapped to nested structure
 * }</pre>
 * 
 * <h3>2. Transform API Request to Canonical:</h3>
 * 
 * <pre>{@code
 * TransactionRequest request = // ... from REST API
 * CanonicalFraudEvent canonical = mapper.fromRequest(request);
 * // canonical now has v2 schema with nested structure
 * }</pre>
 * 
 * <h3>3. Enrich Canonical Event:</h3>
 * 
 * <pre>{@code
 * CanonicalFraudEvent canonical = // ... from any source
 * CanonicalFraudEvent enriched = mapper.enrich(canonical);
 * // enriched now has behavioral metrics and fraud signals populated
 * }</pre>
 * 
 * <h3>4. Convert Back to Legacy Format:</h3>
 * 
 * <pre>{@code
 * CanonicalFraudEvent canonical = // ... internal format
 * TransactionEvent legacy = mapper.toLegacy(canonical);
 * // legacy now has flat structure for backward compatibility
 * }</pre>
 * 
 * @see CanonicalFraudEvent
 * @see TransactionEvent
 * @see TransactionRequest
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventMapper {

    private final TransactionRepository transactionRepository;

    /**
     * Transform legacy flat TransactionEvent (v1) to CanonicalFraudEvent.
     * 
     * <p>
     * Maps flat fields to nested structure while preserving all data.
     * Sets schemaVersion to "v1" to indicate legacy format.
     * </p>
     * 
     * @param event legacy transaction event from Kafka
     * @return canonical fraud event with v1 schema
     * @throws IllegalArgumentException if event is null
     */
    public CanonicalFraudEvent fromLegacy(TransactionEvent event) {
        if (event == null) {
            log.warn("Attempted to transform null TransactionEvent");
            throw new IllegalArgumentException("TransactionEvent cannot be null");
        }

        log.debug("Transforming legacy TransactionEvent to CanonicalFraudEvent: txnId={}", event.getTxnId());

        try {
            CanonicalFraudEvent canonical = CanonicalFraudEvent.builder()
                    .txnId(event.getTxnId())
                    .schemaVersion("v1")
                    .eventTimestamp(event.getEventTime() != null ? event.getEventTime() : LocalDateTime.now())
                    .customer(buildCustomerInfoFromLegacy(event))
                    .transaction(buildTransactionInfoFromLegacy(event))
                    .merchant(buildMerchantInfoFromLegacy(event))
                    .device(buildDeviceInfoFromLegacy(event))
                    .location(buildLocationInfoFromLegacy(event))
                    .build();

            log.debug("Successfully transformed legacy event: txnId={}, schemaVersion=v1", event.getTxnId());
            return canonical;

        } catch (Exception e) {
            log.error("Error transforming legacy TransactionEvent: txnId={}", event.getTxnId(), e);
            throw new RuntimeException("Failed to transform legacy event", e);
        }
    }

    /**
     * Transform flat TransactionRequest (API) to CanonicalFraudEvent.
     * 
     * <p>
     * Maps API request fields to nested structure.
     * Sets schemaVersion to "v2" (default nested format).
     * </p>
     * 
     * @param request transaction request from REST API
     * @return canonical fraud event with v2 schema
     * @throws IllegalArgumentException if request is null
     */
    public CanonicalFraudEvent fromRequest(TransactionRequest request) {
        if (request == null) {
            log.warn("Attempted to transform null TransactionRequest");
            throw new IllegalArgumentException("TransactionRequest cannot be null");
        }

        log.debug("Transforming TransactionRequest to CanonicalFraudEvent: txnId={}", request.getTxnId());

        try {
            CanonicalFraudEvent canonical = CanonicalFraudEvent.builder()
                    .txnId(request.getTxnId())
                    .schemaVersion("v2")
                    .eventTimestamp(LocalDateTime.now())
                    .customer(buildCustomerInfoFromRequest(request))
                    .transaction(buildTransactionInfoFromRequest(request))
                    .merchant(buildMerchantInfoFromRequest(request))
                    .device(buildDeviceInfoFromRequest(request))
                    .location(buildLocationInfoFromRequest(request))
                    .metadata(buildMetadataInfo())
                    .build();

            log.debug("Successfully transformed API request: txnId={}, schemaVersion=v2", request.getTxnId());
            return canonical;

        } catch (Exception e) {
            log.error("Error transforming TransactionRequest: txnId={}", request.getTxnId(), e);
            throw new RuntimeException("Failed to transform API request", e);
        }
    }

    /**
     * Transform TransactionEventV2 (v2 nested format) to CanonicalFraudEvent.
     *
     * <p>
     * Direct mapping from v2 nested structure to canonical format.
     * This is the most efficient transformation as both use the same nested
     * structure.
     * </p>
     *
     * @param eventV2 transaction event v2 from Kafka
     * @return canonical fraud event with v2 schema
     * @throws IllegalArgumentException if eventV2 is null
     */
    public CanonicalFraudEvent fromV2(TransactionEventV2 eventV2) {
        if (eventV2 == null) {
            log.warn("Attempted to transform null TransactionEventV2");
            throw new IllegalArgumentException("TransactionEventV2 cannot be null");
        }

        log.debug("Transforming TransactionEventV2 to CanonicalFraudEvent: txnId={}", eventV2.getTxnId());

        try {
            CanonicalFraudEvent canonical = CanonicalFraudEvent.builder()
                    .txnId(eventV2.getTxnId())
                    .schemaVersion(eventV2.getSchemaVersion() != null ? eventV2.getSchemaVersion() : "v2")
                    .eventTimestamp(
                            eventV2.getEventTimestamp() != null ? eventV2.getEventTimestamp() : LocalDateTime.now())
                    .customer(eventV2.getCustomer())
                    .transaction(eventV2.getTransaction())
                    .merchant(eventV2.getMerchant())
                    .device(eventV2.getDevice())
                    .location(eventV2.getLocation())
                    .behaviorMetrics(eventV2.getBehaviorMetrics())
                    .fraudSignals(eventV2.getFraudSignals())
                    .metadata(eventV2.getMetadata())
                    .build();

            log.debug("Successfully transformed v2 event: txnId={}, schemaVersion={}",
                    eventV2.getTxnId(), canonical.getSchemaVersion());
            return canonical;

        } catch (Exception e) {
            log.error("Error transforming TransactionEventV2: txnId={}", eventV2.getTxnId(), e);
            throw new RuntimeException("Failed to transform v2 event", e);
        }
    }

    /**
     * Transform CanonicalFraudEvent to TransactionEventV2 (v2 nested format).
     *
     * <p>
     * Direct mapping from canonical format to v2 nested structure.
     * Useful for publishing enriched events back to Kafka.
     * </p>
     *
     * @param canonical canonical fraud event
     * @return transaction event v2 with nested structure
     * @throws IllegalArgumentException if canonical is null
     */
    public TransactionEventV2 toV2(CanonicalFraudEvent canonical) {
        if (canonical == null) {
            log.warn("Attempted to transform null CanonicalFraudEvent to v2");
            throw new IllegalArgumentException("CanonicalFraudEvent cannot be null");
        }

        log.debug("Transforming CanonicalFraudEvent to TransactionEventV2: txnId={}", canonical.getTxnId());

        try {
            TransactionEventV2 eventV2 = TransactionEventV2.builder()
                    .txnId(canonical.getTxnId())
                    .schemaVersion(canonical.getSchemaVersion() != null ? canonical.getSchemaVersion() : "v2")
                    .eventTimestamp(canonical.getEventTimestamp())
                    .customer(canonical.getCustomer())
                    .transaction(canonical.getTransaction())
                    .merchant(canonical.getMerchantInfo())
                    .device(canonical.getDevice())
                    .location(canonical.getLocation())
                    .behaviorMetrics(canonical.getBehaviorMetrics())
                    .fraudSignals(canonical.getFraudSignals())
                    .metadata(canonical.getMetadata())
                    .build();

            log.debug("Successfully transformed to v2 format: txnId={}", canonical.getTxnId());
            return eventV2;

        } catch (Exception e) {
            log.error("Error transforming CanonicalFraudEvent to v2: txnId={}", canonical.getTxnId(), e);
            throw new RuntimeException("Failed to transform to v2 format", e);
        }
    }

    /**
     * Transform CanonicalFraudEvent to legacy TransactionEvent (for backward
     * compatibility).
     * 
     * <p>
     * Flattens nested structure back to legacy flat format.
     * Useful for systems that still expect the old format.
     * </p>
     * 
     * @param canonical canonical fraud event
     * @return legacy transaction event with flat structure
     * @throws IllegalArgumentException if canonical is null
     */
    public TransactionEvent toLegacy(CanonicalFraudEvent canonical) {
        if (canonical == null) {
            log.warn("Attempted to transform null CanonicalFraudEvent to legacy");
            throw new IllegalArgumentException("CanonicalFraudEvent cannot be null");
        }

        log.debug("Transforming CanonicalFraudEvent to legacy TransactionEvent: txnId={}", canonical.getTxnId());

        try {
            TransactionEvent legacy = TransactionEvent.builder()
                    .txnId(canonical.getTxnId())
                    .customerId(canonical.getCustomerId())
                    .amount(canonical.getAmount())
                    .merchant(canonical.getMerchant())
                    .country(canonical.getCountry())
                    .deviceId(canonical.getDeviceId())
                    .paymentType(canonical.getPaymentType())
                    .timestamp(canonical.getTimestamp())
                    .eventTime(canonical.getEventTimestamp())
                    .build();

            log.debug("Successfully transformed to legacy format: txnId={}", canonical.getTxnId());
            return legacy;

        } catch (Exception e) {
            log.error("Error transforming CanonicalFraudEvent to legacy: txnId={}", canonical.getTxnId(), e);
            throw new RuntimeException("Failed to transform to legacy format", e);
        }
    }

    /**
     * Enrich CanonicalFraudEvent with additional data.
     * 
     * <p>
     * Enrichment includes:
     * </p>
     * <ul>
     * <li>Customer transaction history and behavioral metrics</li>
     * <li>Device trust indicators</li>
     * <li>Velocity calculations (24h transaction count and amount)</li>
     * <li>Initial fraud signal detection</li>
     * </ul>
     * 
     * <p>
     * This method is idempotent and safe to call multiple times.
     * If enrichment fails, returns the original event with a warning log.
     * </p>
     * 
     * @param event canonical fraud event to enrich
     * @return enriched canonical fraud event
     */
    public CanonicalFraudEvent enrich(CanonicalFraudEvent event) {
        if (event == null) {
            log.warn("Attempted to enrich null CanonicalFraudEvent");
            return null;
        }

        log.debug("Enriching CanonicalFraudEvent: txnId={}", event.getTxnId());

        try {
            // Enrich customer information with historical data
            enrichCustomerInfo(event);

            // Calculate behavioral metrics
            BehaviorMetrics behaviorMetrics = calculateBehaviorMetrics(event);
            event.setBehaviorMetrics(behaviorMetrics);

            // Detect initial fraud signals
            FraudSignals fraudSignals = detectInitialFraudSignals(event);
            event.setFraudSignals(fraudSignals);

            log.debug("Successfully enriched event: txnId={}", event.getTxnId());
            return event;

        } catch (Exception e) {
            log.warn("Error enriching CanonicalFraudEvent, returning original: txnId={}", event.getTxnId(), e);
            return event; // Graceful degradation
        }
    }

    // ==================== Private Helper Methods ====================

    private CustomerInfo buildCustomerInfoFromLegacy(TransactionEvent event) {
        return CustomerInfo.builder()
                .customerId(event.getCustomerId())
                .build();
    }

    private TransactionInfo buildTransactionInfoFromLegacy(TransactionEvent event) {
        return TransactionInfo.builder()
                .amount(event.getAmount())
                .paymentType(event.getPaymentType())
                .timestamp(event.getTimestamp())
                .build();
    }

    private MerchantInfo buildMerchantInfoFromLegacy(TransactionEvent event) {
        return MerchantInfo.builder()
                .merchantName(event.getMerchant())
                .build();
    }

    private DeviceInfo buildDeviceInfoFromLegacy(TransactionEvent event) {
        return DeviceInfo.builder()
                .deviceId(event.getDeviceId())
                .build();
    }

    private LocationInfo buildLocationInfoFromLegacy(TransactionEvent event) {
        return LocationInfo.builder()
                .country(event.getCountry())
                .build();
    }

    private CustomerInfo buildCustomerInfoFromRequest(TransactionRequest request) {
        return CustomerInfo.builder()
                .customerId(request.getCustomerId())
                .build();
    }

    private TransactionInfo buildTransactionInfoFromRequest(TransactionRequest request) {
        return TransactionInfo.builder()
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now())
                .build();
    }

    private MerchantInfo buildMerchantInfoFromRequest(TransactionRequest request) {
        return MerchantInfo.builder()
                .merchantName(request.getMerchant())
                .build();
    }

    private DeviceInfo buildDeviceInfoFromRequest(TransactionRequest request) {
        return DeviceInfo.builder()
                .deviceId(request.getDeviceId())
                .build();
    }

    private LocationInfo buildLocationInfoFromRequest(TransactionRequest request) {
        return LocationInfo.builder()
                .country(request.getCountry())
                .build();
    }

    private MetadataInfo buildMetadataInfo() {
        return MetadataInfo.builder()
                .timestamp(LocalDateTime.now())
                .apiVersion("v2")
                .build();
    }

    private void enrichCustomerInfo(CanonicalFraudEvent event) {
        if (event.getCustomer() == null || event.getCustomer().getCustomerId() == null) {
            log.debug("Skipping customer enrichment: customer info not available");
            return;
        }

        try {
            String customerId = event.getCustomer().getCustomerId();

            // Get customer transaction history
            List<com.fraud.platform.entity.Transaction> customerTransactions = transactionRepository
                    .findByCustomerIdOrderByTimestampDesc(customerId);

            if (!customerTransactions.isEmpty()) {
                event.getCustomer().setTotalTransactions((long) customerTransactions.size());

                // Calculate average transaction amount
                BigDecimal avgAmount = customerTransactions.stream()
                        .map(com.fraud.platform.entity.Transaction::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(customerTransactions.size()), 2, RoundingMode.HALF_UP);

                event.getCustomer().setAvgTransactionAmount(avgAmount);
                event.getCustomer().setLastActivityDate(customerTransactions.get(0).getTimestamp());

                log.debug("Enriched customer info: customerId={}, totalTxns={}, avgAmount={}",
                        customerId, customerTransactions.size(), avgAmount);
            }

        } catch (Exception e) {
            log.warn("Error enriching customer info: customerId={}", event.getCustomer().getCustomerId(), e);
        }
    }

    private BehaviorMetrics calculateBehaviorMetrics(CanonicalFraudEvent event) {
        BehaviorMetrics.BehaviorMetricsBuilder builder = BehaviorMetrics.builder();

        if (event.getCustomer() == null || event.getCustomer().getCustomerId() == null) {
            log.debug("Skipping behavior metrics: customer info not available");
            return builder.build();
        }

        try {
            String customerId = event.getCustomer().getCustomerId();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twentyFourHoursAgo = now.minusHours(24);

            // Count transactions in last 24 hours
            Long txnCount24h = transactionRepository.countByCustomerIdAndTimestampBetween(
                    customerId, twentyFourHoursAgo, now);
            builder.transactionCount24h(txnCount24h != null ? txnCount24h.intValue() : 0);

            // Calculate velocity score (simple: txn count / 24)
            if (txnCount24h != null && txnCount24h > 0) {
                BigDecimal velocityScore = BigDecimal.valueOf(txnCount24h)
                        .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
                builder.velocityScore(velocityScore);
            }

            // Check for unusual amount
            if (event.getCustomer().getAvgTransactionAmount() != null && event.getAmount() != null) {
                BigDecimal avgAmount = event.getCustomer().getAvgTransactionAmount();
                BigDecimal currentAmount = event.getAmount();

                // Flag as unusual if > 3x average
                boolean unusualAmount = currentAmount.compareTo(avgAmount.multiply(BigDecimal.valueOf(3))) > 0;
                builder.unusualAmount(unusualAmount);

                builder.avgTransactionAmount(avgAmount);
            }

            log.debug("Calculated behavior metrics: customerId={}, txnCount24h={}", customerId, txnCount24h);

        } catch (Exception e) {
            log.warn("Error calculating behavior metrics: customerId={}", event.getCustomer().getCustomerId(), e);
        }

        return builder.build();
    }

    private FraudSignals detectInitialFraudSignals(CanonicalFraudEvent event) {
        FraudSignals.FraudSignalsBuilder builder = FraudSignals.builder();
        List<String> suspiciousPatterns = new ArrayList<>();

        try {
            // Check device trust
            if (event.getDevice() != null) {
                builder.vpnDetected(event.getDevice().getVpnDetected());
                builder.proxyDetected(event.getDevice().getProxyDetected());

                if (Boolean.TRUE.equals(event.getDevice().getVpnDetected())) {
                    suspiciousPatterns.add("VPN_DETECTED");
                }
                if (Boolean.TRUE.equals(event.getDevice().getProxyDetected())) {
                    suspiciousPatterns.add("PROXY_DETECTED");
                }
            }

            // Check merchant blacklist
            MerchantInfo merchantInfo = event.getMerchantInfo();
            if (merchantInfo != null && Boolean.TRUE.equals(merchantInfo.getIsBlacklisted())) {
                builder.blacklistedMerchant(true);
                suspiciousPatterns.add("BLACKLISTED_MERCHANT");
            }

            // Check for high velocity
            if (event.getBehaviorMetrics() != null && event.getBehaviorMetrics().getTransactionCount24h() != null) {
                if (event.getBehaviorMetrics().getTransactionCount24h() > 10) {
                    suspiciousPatterns.add("HIGH_VELOCITY");
                }
            }

            // Check for unusual amount
            if (event.getBehaviorMetrics() != null
                    && Boolean.TRUE.equals(event.getBehaviorMetrics().getUnusualAmount())) {
                suspiciousPatterns.add("UNUSUAL_AMOUNT");
            }

            builder.suspiciousPatterns(suspiciousPatterns);

            // Calculate initial risk score (0-100)
            int riskScore = calculateInitialRiskScore(event, suspiciousPatterns.size());
            builder.riskScore(riskScore);

            log.debug("Detected initial fraud signals: txnId={}, patterns={}, riskScore={}",
                    event.getTxnId(), suspiciousPatterns.size(), riskScore);

        } catch (Exception e) {
            log.warn("Error detecting fraud signals: txnId={}", event.getTxnId(), e);
        }

        return builder.build();
    }

    private int calculateInitialRiskScore(CanonicalFraudEvent event, int patternCount) {
        int score = 0;

        // Base score from pattern count (10 points per pattern, max 50)
        score += Math.min(patternCount * 10, 50);

        // Add score for high velocity
        if (event.getBehaviorMetrics() != null && event.getBehaviorMetrics().getTransactionCount24h() != null) {
            int txnCount = event.getBehaviorMetrics().getTransactionCount24h();
            if (txnCount > 10) {
                score += Math.min((txnCount - 10) * 2, 20);
            }
        }

        // Add score for unusual amount
        if (event.getBehaviorMetrics() != null && Boolean.TRUE.equals(event.getBehaviorMetrics().getUnusualAmount())) {
            score += 15;
        }

        // Add score for blacklisted merchant
        MerchantInfo merchantInfo = event.getMerchantInfo();
        if (merchantInfo != null && Boolean.TRUE.equals(merchantInfo.getIsBlacklisted())) {
            score += 25;
        }

        // Cap at 100
        return Math.min(score, 100);
    }
}

// Made with Bob