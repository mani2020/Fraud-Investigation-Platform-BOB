package com.fraud.platform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * System health status model for monitoring platform components.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealth {
    
    private String status; // HEALTHY, DEGRADED, DOWN
    private LocalDateTime timestamp;
    private KafkaHealth kafka;
    private DatabaseHealth database;
    private AgentsHealth agents;
    private ApiHealth api;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KafkaHealth {
        private String status; // CONNECTED, DISCONNECTED, ERROR
        private String message;
        private Long lastMessageTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseHealth {
        private String status; // HEALTHY, SLOW, DOWN
        private String message;
        private Long responseTimeMs;
        private Long transactionCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentsHealth {
        private String status; // ALL_ACTIVE, PARTIAL, DOWN
        private Integer activeCount;
        private Integer totalCount;
        private List<AgentStatus> agents;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentStatus {
        private String name;
        private String status; // ACTIVE, INACTIVE, ERROR
        private Integer successRate; // 0-100
        private Long lastExecutionTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiHealth {
        private String status; // OPERATIONAL, DEGRADED, DOWN
        private String message;
        private Long uptime;
    }
}

// Made with Bob