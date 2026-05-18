package com.fraud.platform.entity.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraud.platform.model.nested.DeviceInfo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for DeviceInfo to JSONB
 * Converts DeviceInfo objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class DeviceInfoConverter implements AttributeConverter<DeviceInfo, String> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoConverter.class);
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

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
