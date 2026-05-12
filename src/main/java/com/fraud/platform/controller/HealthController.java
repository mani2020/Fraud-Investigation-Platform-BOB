package com.fraud.platform.controller;

import com.fraud.platform.model.SystemHealth;
import com.fraud.platform.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for system health monitoring.
 * Provides endpoints for checking platform component status.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HealthController {

    private final HealthCheckService healthCheckService;

    /**
     * Get comprehensive system health status.
     *
     * @return System health information
     */
    @GetMapping
    public ResponseEntity<SystemHealth> getSystemHealth() {
        log.debug("Health check requested");
        SystemHealth health = healthCheckService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Simple ping endpoint for basic availability check.
     *
     * @return OK status
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Get detailed health status (alias for main endpoint).
     *
     * @return System health information
     */
    @GetMapping("/status")
    public ResponseEntity<SystemHealth> getStatus() {
        return getSystemHealth();
    }
}

// Made with Bob