package com.fraud.platform.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.model.nested.DeviceInfo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter for DeviceInfo to JSONB
 * Converts DeviceInfo objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class DeviceInfoConverter implements AttributeConverter<DeviceInfo, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(DeviceInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted DeviceInfo to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting DeviceInfo to JSONB", e);
            throw new IllegalArgumentException("Error converting DeviceInfo to JSONB", e);
        }
    }
    
    @Override
    public DeviceInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            DeviceInfo deviceInfo = objectMapper.readValue(dbData, DeviceInfo.class);
            logger.debug("Converted JSONB to DeviceInfo: {}", deviceInfo);
            return deviceInfo;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to DeviceInfo: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to DeviceInfo", e);
        }
    }
}

// Made with Bob
