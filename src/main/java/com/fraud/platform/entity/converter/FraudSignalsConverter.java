package com.fraud.platform.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.model.nested.FraudSignals;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter for FraudSignals to JSONB
 * Converts FraudSignals objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class FraudSignalsConverter implements AttributeConverter<FraudSignals, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudSignalsConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(FraudSignals attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted FraudSignals to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting FraudSignals to JSONB", e);
            throw new IllegalArgumentException("Error converting FraudSignals to JSONB", e);
        }
    }
    
    @Override
    public FraudSignals convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            FraudSignals fraudSignals = objectMapper.readValue(dbData, FraudSignals.class);
            logger.debug("Converted JSONB to FraudSignals: {}", fraudSignals);
            return fraudSignals;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to FraudSignals: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to FraudSignals", e);
        }
    }
}

// Made with Bob
