package io.wanjune.agent.config;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.wanjune.agent.config.properties.LlmProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * @author zjh
 * @since 2026/2/18 12:09
 */
@Configuration
public class LlmConfig {
    @Bean
    ChatLanguageModel chatLanguageModel(LlmProperties p) {
        return OpenAiChatModel.builder()
                .baseUrl(p.baseUrl())
                .apiKey(p.apiKey())
                .modelName(p.model())
                .temperature(p.temperature() == null ? 0.2 : p.temperature())
                .timeout(Duration.ofSeconds(p.timeoutSeconds() == null ? 60 : p.timeoutSeconds()))
                .build();
    }

    /**
     * 自动收集所有含 @Tool 方法的 Spring Bean，注入到 AgentService.
     */
    @Bean
    List<Object> tools(ApplicationContext ctx) {
        return ctx.getBeansWithAnnotation(org.springframework.stereotype.Component.class)
                .values().stream()
                .filter(bean -> java.util.Arrays.stream(bean.getClass().getDeclaredMethods())
                        .anyMatch(m -> m.isAnnotationPresent(Tool.class)))
                .map(bean -> (Object) bean)
                .toList();
    }

}