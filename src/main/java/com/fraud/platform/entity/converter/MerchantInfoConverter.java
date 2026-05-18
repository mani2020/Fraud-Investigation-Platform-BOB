package com.fraud.platform.entity.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraud.platform.model.nested.MerchantInfo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for MerchantInfo to JSONB
 * Converts MerchantInfo objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class MerchantInfoConverter implements AttributeConverter<MerchantInfo, String> {

    private static final Logger logger = LoggerFactory.getLogger(MerchantInfoConverter.class);
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Override
    public String convertToDatabaseColumn(MerchantInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted MerchantInfo to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting MerchantInfo to JSONB", e);
            throw new IllegalArgumentException("Error converting MerchantInfo to JSONB", e);
        }
    }

    @Override
    public MerchantInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            MerchantInfo merchantInfo = objectMapper.readValue(dbData, MerchantInfo.class);
            logger.debug("Converted JSONB to MerchantInfo: {}", merchantInfo);
            return merchantInfo;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to MerchantInfo: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to MerchantInfo", e);
        }
    }
}

// Made with Bob
