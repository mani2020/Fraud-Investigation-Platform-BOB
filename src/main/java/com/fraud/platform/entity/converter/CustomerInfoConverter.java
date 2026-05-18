package com.fraud.platform.entity.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraud.platform.model.nested.CustomerInfo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for CustomerInfo to JSONB
 * Converts CustomerInfo objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class CustomerInfoConverter implements AttributeConverter<CustomerInfo, String> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerInfoConverter.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public String convertToDatabaseColumn(CustomerInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted CustomerInfo to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting CustomerInfo to JSONB", e);
            throw new IllegalArgumentException("Error converting CustomerInfo to JSONB", e);
        }
    }

    @Override
    public CustomerInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            CustomerInfo customerInfo = objectMapper.readValue(dbData, CustomerInfo.class);
            logger.debug("Converted JSONB to CustomerInfo: {}", customerInfo);
            return customerInfo;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to CustomerInfo: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to CustomerInfo", e);
        }
    }
}

// Made with Bob
