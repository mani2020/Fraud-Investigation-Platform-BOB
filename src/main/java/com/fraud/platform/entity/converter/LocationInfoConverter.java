package com.fraud.platform.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.model.nested.LocationInfo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter for LocationInfo to JSONB
 * Converts LocationInfo objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class LocationInfoConverter implements AttributeConverter<LocationInfo, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationInfoConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(LocationInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted LocationInfo to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting LocationInfo to JSONB", e);
            throw new IllegalArgumentException("Error converting LocationInfo to JSONB", e);
        }
    }
    
    @Override
    public LocationInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            LocationInfo locationInfo = objectMapper.readValue(dbData, LocationInfo.class);
            logger.debug("Converted JSONB to LocationInfo: {}", locationInfo);
            return locationInfo;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to LocationInfo: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to LocationInfo", e);
        }
    }
}

// Made with Bob
