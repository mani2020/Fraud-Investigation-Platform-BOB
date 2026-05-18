package com.fraud.platform.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraud.platform.kafka.events.TransactionEvent;
import com.fraud.platform.mapper.TransactionEventMapper;
import com.fraud.platform.model.CanonicalFraudEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Version-aware Kafka deserializer for fraud events.
 * 
 * <p>This deserializer automatically detects the schema version and
 * deserializes
 * the JSON payload to CanonicalFraudEvent, supporting both v1 (flat) and v2
 * (nested) formats.</p>
 * 
 * <h2>Version Detection:</h2>
 * <ul>
 * <li><b>v1</b>: Legacy flat structure (no schemaVersion field or
 * schemaVersion="v1")</li>
 * <li><b>v2</b>: Nested structure (schemaVersion="v2")</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <h3>1. Kafka Consumer Configuration:</h3>
 * <pre>{@code
 * Properties props = new Properties();
 * props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
 * StringDeserializer.class);
 * props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
 * FraudEventDeserializer.class);
 * 
 * KafkaConsumer<String, CanonicalFraudEvent> consumer = new
 * KafkaConsumer<>(props);
 * }</pre>
 * 
 * <h3>2. Consuming Messages:</h3>
 * <pre>{@code
 * ConsumerRecords<String, CanonicalFraudEvent> records =
 * consumer.poll(Duration.ofMillis(100));
 * for (ConsumerRecord<String, CanonicalFraudEvent> record : records) {
 * CanonicalFraudEvent event = record.value();
 * // event is automatically deserialized with correct version
 * if (event.isLegacyEvent()) {
 * // Handle v1 format
 * } else {
 * // Handle v2 format
 * }
 * }
 * }</pre>
 * 
 * <h3>3. Version Detection:</h3>
 * <pre>{@code
 * // V1 JSON (no schemaVersion field):
 * {
 * "txnId": "TXN-001",
 * "customerId": "CUST-001",
 * "amount": 1500.00,
 * ...
 * }
 * // Detected as v1, deserialized to CanonicalFraudEvent with
 * schemaVersion="v1"
 * 
 * // V2 JSON (with schemaVersion field):
 * {
 * "txnId": "TXN-001",
 * "schemaVersion": "v2",
 * "customer": { "customerId": "CUST-001", ... },
 * "transaction": { "amount": 1500.00, ... },
 * ...
 * }
 * // Detected as v2, deserialized to CanonicalFraudEvent with
 * schemaVersion="v2"
 * }</pre>
 * 
 * @see CanonicalFraudEvent
 * @see TransactionEventMapper
 */
@Slf4j
public class FraudEventDeserializer implements Deserializer<CanonicalFraudEvent> {

    private final ObjectMapper objectMapper;
    private TransactionEventMapper mapper;

    /**
     * Default constructor.
     * Initializes ObjectMapper with JavaTimeModule for LocalDateTime support.
     */
    public FraudEventDeserializer() {
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .findAndAddModules()
                .build();
        log.debug("FraudEventDeserializer initialized");
    }

    /**
     * Configure the deserializer.
     * 
     * @param configs configuration map
     * @param isKey   whether this is for key or value deserialization
     */
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Extract mapper from configs if provided (for Spring integration)
        Object mapperObj = configs.get("transaction.event.mapper");
        if (mapperObj instanceof TransactionEventMapper) {
            this.mapper = (TransactionEventMapper) mapperObj;
            log.debug("TransactionEventMapper configured from configs");
        }
        log.debug("FraudEventDeserializer configured: isKey={}", isKey);
    }

    /**
     * Deserialize JSON to CanonicalFraudEvent with version detection.
     * 
     * <p>
     * This method:
     * </p>
     * <ol>
     * <li>Detects the schema version from the JSON</li>
     * <li>Deserializes using the appropriate method (v1 or v2)</li>
     * <li>Returns a CanonicalFraudEvent with the correct schema version</li>
     * </ol>
     * 
     * @param topic Kafka topic name
     * @param data  JSON byte array
     * @return deserialized CanonicalFraudEvent
     * @throws SerializationException if deserialization fails
     */
    @Override
    public CanonicalFraudEvent deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            log.warn("Received null or empty data from topic: {}", topic);
            return null;
        }

        try {
            String json = new String(data, StandardCharsets.UTF_8);
            log.debug("Deserializing message from topic: {}, size: {} bytes", topic, data.length);

            // Detect version
            String version = detectVersion(json);
            log.debug("Detected schema version: {}", version);

            // Deserialize based on version
            CanonicalFraudEvent event;
            if ("v1".equals(version)) {
                event = deserializeV1(json);
            } else {
                event = deserializeV2(json);
            }

            log.debug("Successfully deserialized event: txnId={}, version={}",
                    event.getTxnId(), event.getSchemaVersion());
            return event;

        } catch (Exception e) {
            log.error("Error deserializing message from topic: {}", topic, e);
            throw new SerializationException("Failed to deserialize CanonicalFraudEvent", e);
        }
    }

    /**
     * Detect schema version from JSON.
     * 
     * <p>
     * Detection logic:
     * </p>
     * <ul>
     * <li>If schemaVersion field exists and equals "v2", return "v2"</li>
     * <li>If schemaVersion field exists and equals "v1", return "v1"</li>
     * <li>If schemaVersion field is missing, return "v1" (legacy format)</li>
     * </ul>
     * 
     * @param json JSON string
     * @return detected version ("v1" or "v2")
     */
    private String detectVersion(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // Check for schemaVersion field
            if (root.has("schemaVersion")) {
                String version = root.get("schemaVersion").asText();
                log.debug("Found schemaVersion field: {}", version);
                return version;
            }

            // Check for nested structure indicators (customer, transaction objects)
            if (root.has("customer") && root.get("customer").isObject()) {
                log.debug("Detected nested structure (customer object), assuming v2");
                return "v2";
            }

            // Default to v1 for backward compatibility
            log.debug("No schemaVersion field found, defaulting to v1");
            return "v1";

        } catch (Exception e) {
            log.warn("Error detecting version, defaulting to v1", e);
            return "v1";
        }
    }

    /**
     * Deserialize v1 format (flat structure).
     * 
     * <p>
     * Deserializes legacy TransactionEvent and transforms it to CanonicalFraudEvent
     * using TransactionEventMapper.
     * </p>
     * 
     * @param json JSON string in v1 format
     * @return CanonicalFraudEvent with v1 schema
     * @throws Exception if deserialization fails
     */
    private CanonicalFraudEvent deserializeV1(String json) throws Exception {
        log.debug("Deserializing v1 format (flat structure)");

        // Deserialize to legacy TransactionEvent
        TransactionEvent legacyEvent = objectMapper.readValue(json, TransactionEvent.class);

        // Transform to CanonicalFraudEvent if mapper is available
        if (mapper != null) {
            log.debug("Transforming legacy event using mapper");
            return mapper.fromLegacy(legacyEvent);
        }

        // Fallback: manual transformation if mapper not available
        log.debug("Mapper not available, performing manual transformation");
        return CanonicalFraudEvent.builder()
                .txnId(legacyEvent.getTxnId())
                .schemaVersion("v1")
                .eventTimestamp(legacyEvent.getEventTime())
                .customer(com.fraud.platform.model.nested.CustomerInfo.builder()
                        .customerId(legacyEvent.getCustomerId())
                        .build())
                .transaction(com.fraud.platform.model.nested.TransactionInfo.builder()
                        .amount(legacyEvent.getAmount())
                        .paymentType(legacyEvent.getPaymentType())
                        .timestamp(legacyEvent.getTimestamp())
                        .build())
                .merchant(com.fraud.platform.model.nested.MerchantInfo.builder()
                        .merchantName(legacyEvent.getMerchant())
                        .build())
                .device(com.fraud.platform.model.nested.DeviceInfo.builder()
                        .deviceId(legacyEvent.getDeviceId())
                        .build())
                .location(com.fraud.platform.model.nested.LocationInfo.builder()
                        .country(legacyEvent.getCountry())
                        .build())
                .build();
    }

    /**
     * Deserialize v2 format (nested structure).
     * 
     * <p>
     * Directly deserializes JSON to CanonicalFraudEvent using Jackson.
     * </p>
     * 
     * @param json JSON string in v2 format
     * @return CanonicalFraudEvent with v2 schema
     * @throws Exception if deserialization fails
     */
    private CanonicalFraudEvent deserializeV2(String json) throws Exception {
        log.debug("Deserializing v2 format (nested structure)");
        return objectMapper.readValue(json, CanonicalFraudEvent.class);
    }

    /**
     * Close the deserializer.
     * Performs cleanup if needed.
     */
    @Override
    public void close() {
        log.debug("FraudEventDeserializer closed");
        // No resources to clean up
    }

    /**
     * Set the TransactionEventMapper for v1 to v2 transformation.
     * 
     * <p>
     * This method is useful for Spring integration where the mapper
     * can be injected after construction.
     * </p>
     * 
     * @param mapper the transaction event mapper
     */
    public void setMapper(TransactionEventMapper mapper) {
        this.mapper = mapper;
        log.debug("TransactionEventMapper set");
    }
}

// Made with Bob