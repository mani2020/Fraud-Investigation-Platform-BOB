package com.fraud.platform.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.platform.model.nested.MetadataInfo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter for MetadataInfo to JSONB
 * Converts MetadataInfo objects to PostgreSQL JSONB type for database storage
 * and vice versa for entity retrieval.
 */
@Converter
public class MetadataInfoConverter implements AttributeConverter<MetadataInfo, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetadataInfoConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(MetadataInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(attribute);
            logger.debug("Converted MetadataInfo to JSONB: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error converting MetadataInfo to JSONB", e);
            throw new IllegalArgumentException("Error converting MetadataInfo to JSONB", e);
        }
    }
    
    @Override
    public MetadataInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            MetadataInfo metadataInfo = objectMapper.readValue(dbData, MetadataInfo.class);
            logger.debug("Converted JSONB to MetadataInfo: {}", metadataInfo);
            return metadataInfo;
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSONB to MetadataInfo: {}", dbData, e);
            throw new IllegalArgumentException("Error converting JSONB to MetadataInfo", e);
        }
    }
}

// Made with Bob
