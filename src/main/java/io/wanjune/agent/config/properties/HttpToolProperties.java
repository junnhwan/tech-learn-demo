package io.wanjune.agent.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author zjh
 * @since 2026/2/18 18:24
 */
@ConfigurationProperties(prefix = "tools.http")
public record HttpToolProperties(
        List<String> allowedHosts,
        Integer timeoutSeconds,
        Integer maxBodyBytes
) {}