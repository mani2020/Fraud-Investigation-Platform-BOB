package com.fraud.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;

@Configuration
@Getter
public class ICAConfig {

    @Value("${ica.context-studio.base-url}")
    private String baseUrl;

    @Value("${ica.context-studio.context-id}")
    private String contextId;

    @Value("${ica.context-studio.api-key}")
    private String apiKey;

    @Value("${ica.context-studio.mcp-gateway-token}")
    private String mcpGatewayToken;
}
