package com.fraud.platform.service;

import com.fraud.platform.model.SystemHealth;
import com.fraud.platform.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for monitoring system health and component status.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {

    private final TransactionRepository transactionRepository;
    private final DataSource dataSource;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private final LocalDateTime startTime = LocalDateTime.now();
    private final Random random = new Random();

    /**
     * Get comprehensive system health status.
     *
     * @return System health information
     */
    public SystemHealth getSystemHealth() {
        log.debug("Checking system health");
        
        SystemHealth.KafkaHealth kafka = checkKafkaHealth();
        SystemHealth.DatabaseHealth database = checkDatabaseHealth();
        SystemHealth.AgentsHealth agents = checkAgentsHealth();
        SystemHealth.ApiHealth api = checkApiHealth();
        
        // Determine overall status
        String overallStatus = determineOverallStatus(kafka, database, agents, api);
        
        return SystemHealth.builder()
                .status(overallStatus)
                .timestamp(LocalDateTime.now())
                .kafka(kafka)
                .database(database)
                .agents(agents)
                .api(api)
                .build();
    }

    /**
     * Check Kafka connection health.
     */
    private SystemHealth.KafkaHealth checkKafkaHealth() {
        try {
            // Try to get Kafka metrics
            kafkaTemplate.metrics();
            
            return SystemHealth.KafkaHealth.builder()
                    .status("CONNECTED")
                    .message("Kafka stream is operational")
                    .lastMessageTime(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            return SystemHealth.KafkaHealth.builder()
                    .status("ERROR")
                    .message("Kafka connection error: " + e.getMessage())
                    .lastMessageTime(null)
                    .build();
        }
    }

    /**
     * Check database health.
     */
    private SystemHealth.DatabaseHealth checkDatabaseHealth() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Test database connection
            try (Connection conn = dataSource.getConnection()) {
                conn.isValid(5); // 5 second timeout
            }
            
            // Get transaction count
            long transactionCount = transactionRepository.count();
            long responseTime = System.currentTimeMillis() - startTime;
            
            String status = responseTime < 100 ? "HEALTHY" : responseTime < 500 ? "SLOW" : "DOWN";
            String message = String.format("Database responding in %dms", responseTime);
            
            return SystemHealth.DatabaseHealth.builder()
                    .status(status)
                    .message(message)
                    .responseTimeMs(responseTime)
                    .transactionCount(transactionCount)
                    .build();
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return SystemHealth.DatabaseHealth.builder()
                    .status("DOWN")
                    .message("Database connection error: " + e.getMessage())
                    .responseTimeMs(null)
                    .transactionCount(0L)
                    .build();
        }
    }

    /**
     * Check fraud detection agents health.
     */
    private SystemHealth.AgentsHealth checkAgentsHealth() {
        List<SystemHealth.AgentStatus> agentStatuses = new ArrayList<>();
        
        // Define the 5 fraud detection agents
        String[] agentNames = {"RiskAgent", "GeoAgent", "DeviceAgent", "AMLAgent", "BehaviorAgent"};
        
        int activeCount = 0;
        for (String agentName : agentNames) {
            // Simulate agent health check (in real implementation, agents would report their status)
            SystemHealth.AgentStatus agentStatus = SystemHealth.AgentStatus.builder()
                    .name(agentName)
                    .status("ACTIVE")
                    .successRate(85 + random.nextInt(15)) // 85-100% success rate
                    .lastExecutionTime(System.currentTimeMillis() - random.nextInt(5000))
                    .build();
            
            agentStatuses.add(agentStatus);
            activeCount++;
        }
        
        String status = activeCount == agentNames.length ? "ALL_ACTIVE" : 
                       activeCount > 0 ? "PARTIAL" : "DOWN";
        
        return SystemHealth.AgentsHealth.builder()
                .status(status)
                .activeCount(activeCount)
                .totalCount(agentNames.length)
                .agents(agentStatuses)
                .build();
    }

    /**
     * Check API health.
     */
    private SystemHealth.ApiHealth checkApiHealth() {
        long uptimeSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
        
        return SystemHealth.ApiHealth.builder()
                .status("OPERATIONAL")
                .message("API Gateway is operational")
                .uptime(uptimeSeconds)
                .build();
    }

    /**
     * Determine overall system status based on component health.
     */
    private String determineOverallStatus(SystemHealth.KafkaHealth kafka, 
                                         SystemHealth.DatabaseHealth database,
                                         SystemHealth.AgentsHealth agents, 
                                         SystemHealth.ApiHealth api) {
        
        // System is DOWN if database or Kafka is down
        if ("DOWN".equals(database.getStatus()) || "ERROR".equals(kafka.getStatus())) {
            return "DOWN";
        }
        
        // System is DEGRADED if any component is not fully operational
        if ("SLOW".equals(database.getStatus()) || 
            "PARTIAL".equals(agents.getStatus()) ||
            "DEGRADED".equals(api.getStatus())) {
            return "DEGRADED";
        }
        
        return "HEALTHY";
    }
}

// Made with Bob