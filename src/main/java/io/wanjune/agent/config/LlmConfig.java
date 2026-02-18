package io.wanjune.agent.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import io.wanjune.agent.tools.TimeTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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

    @Bean
    Assistant assistant(ChatLanguageModel model, TimeTool timeTool) {
        return AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(timeTool)
                .build();
    }

}