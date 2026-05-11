package com.fraud.platform.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * Logging interceptor to track API requests end-to-end
 * Implements industry-standard logging practices:
 * - Request ID generation for correlation
 * - MDC (Mapped Diagnostic Context) for thread-safe logging
 * - Request/Response timing
 * - Structured logging format
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String REQUEST_ID = "requestId";
    private static final String START_TIME = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate unique request ID for correlation
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);
        
        // Store start time for duration calculation
        request.setAttribute(START_TIME, System.currentTimeMillis());
        
        // Add request ID to response header for client tracking
        response.setHeader("X-Request-ID", requestId);
        
        // Log incoming request
        logger.info("=== INCOMING REQUEST ===");
        logger.info("Request ID: {}", requestId);
        logger.info("Method: {}", request.getMethod());
        logger.info("URI: {}", request.getRequestURI());
        logger.info("Query String: {}", request.getQueryString());
        logger.info("Remote Address: {}", request.getRemoteAddr());
        logger.info("User-Agent: {}", request.getHeader("User-Agent"));
        logger.info("Content-Type: {}", request.getContentType());
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // This method is called after the controller method but before view rendering
        // Can be used for additional processing if needed
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            // Calculate request duration
            Long startTime = (Long) request.getAttribute(START_TIME);
            long duration = System.currentTimeMillis() - startTime;
            
            String requestId = MDC.get(REQUEST_ID);
            
            // Log response details
            logger.info("=== OUTGOING RESPONSE ===");
            logger.info("Request ID: {}", requestId);
            logger.info("Status Code: {}", response.getStatus());
            logger.info("Duration: {} ms", duration);
            
            // Log error if present
            if (ex != null) {
                logger.error("Request failed with exception", ex);
            }
            
            // Log performance warning for slow requests
            if (duration > 1000) {
                logger.warn("SLOW REQUEST DETECTED - Duration: {} ms, URI: {}", 
                           duration, request.getRequestURI());
            }
            
            logger.info("=== REQUEST COMPLETED ===");
            
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }
}

// Made with Bob
