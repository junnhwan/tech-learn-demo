package io.wanjune.agent.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * @author zjh
 * @since 2026/2/18 13:38
 */
@Component
public class TimeTool {

    @Tool("Get current server time in ISO-8601 format")
    public String now() {
        return OffsetDateTime.now().toString();
    }

}
