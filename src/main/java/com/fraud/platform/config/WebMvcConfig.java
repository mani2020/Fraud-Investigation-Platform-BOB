package com.fraud.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * Registers the logging interceptor for all API requests
 * Configures CORS to allow frontend requests
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private LoggingInterceptor loggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")  // Apply to all API endpoints
                .excludePathPatterns("/actuator/**");  // Exclude actuator endpoints
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Apply CORS to all API endpoints
                .allowedOriginPatterns("http://localhost:5174")  // Frontend Vite dev server (using patterns for credentials)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allow all common HTTP methods
                .allowedHeaders("*")  // Allow all headers
                .allowCredentials(true)  // Allow credentials (cookies, authorization headers)
                .maxAge(3600);  // Cache preflight response for 1 hour
    }
}

// Made with Bob
