package com.fraud.platform.model.nested;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Nested metadata used for internal fraud analysis payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataInfo {

    /**
     * Distributed tracing identifier.
     */
    private String traceId;

    /**
     * User session identifier.
     */
    private String sessionId;

    /**
     * User agent associated with the request.
     */
    private String userAgent;

    /**
     * Referrer source for the request.
     */
    private String referrer;

    /**
     * API version used by the client.
     */
    private String apiVersion;

    /**
     * Client application version.
     */
    private String clientVersion;

    /**
     * Metadata capture timestamp.
     */
    private LocalDateTime timestamp;
}

// Made with Bob