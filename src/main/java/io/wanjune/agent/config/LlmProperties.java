package io.wanjune.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
        String baseUrl,
        String apiKey,
        String model,
        Integer timeoutSeconds,
        Double temperature
) {}
