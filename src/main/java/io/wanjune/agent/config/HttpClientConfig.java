package io.wanjune.agent.config;

import io.wanjune.agent.config.properties.HttpToolProperties;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author zjh
 * @since 2026/2/18 18:35
 */
@Configuration
public class HttpClientConfig {

    @Bean
    OkHttpClient okHttpClient(HttpToolProperties p) {
        int timeout = p.timeoutSeconds() == null ? 5 : p.timeoutSeconds();
        return new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .callTimeout(Duration.ofSeconds(timeout))
                .connectTimeout(Duration.ofSeconds(timeout))
                .readTimeout(Duration.ofSeconds(timeout))
                .build();
    }

}