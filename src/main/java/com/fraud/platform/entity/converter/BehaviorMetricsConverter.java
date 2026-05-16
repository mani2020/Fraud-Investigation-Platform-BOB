package com.fraud.platform.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.model.nested.BehaviorMetrics;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter for BehaviorMetrics to JSONB
 * Converts BehaviorMetrics objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class BehaviorMetricsConverter implements AttributeConverter<BehaviorMetrics, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(BehaviorMetricsConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(BehaviorMetrics attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted BehaviorMetrics to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting BehaviorMetrics to JSONB", e);
            throw new IllegalArgumentException("Error converting BehaviorMetrics to JSONB", e);
        }
    }
    
    @Override
    public BehaviorMetrics convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            BehaviorMetrics behaviorMetrics = objectMapper.readValue(dbData, BehaviorMetrics.class);
            logger.debug("Converted JSONB to BehaviorMetrics: {}", behaviorMetrics);
            return behaviorMetrics;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to BehaviorMetrics: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to BehaviorMetrics", e);
        }
    }
}

// Made with Bob
