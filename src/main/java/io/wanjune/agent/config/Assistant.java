package io.wanjune.agent.config;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @author zjh
 * @since 2026/2/18 13:31
 */
public interface Assistant {
    @SystemMessage("""
            You are an agent in a backend service.
            Use tools when needed.
            Keep answers concise.
            """)
    String chat(@UserMessage String userMessage);
}
