package io.wanjune.agent.tools;

import dev.langchain4j.agent.tool.Tool;
import io.wanjune.agent.common.StepRecord;
import io.wanjune.agent.common.TraceContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * @author zjh
 * @since 2026/2/18 13:38
 */
@Component
public class TimeTool {

    @Tool("Get current server time in ISO-8601 format")
    public String now() {
        Instant start = Instant.now();
        try {
            String out = OffsetDateTime.now().toString();
            var ctx = TraceContextHolder.get();
            if (ctx != null) {
                ctx.addStep(new StepRecord(
                        ctx.steps().size() + 1,
                        "TOOL",
                        "time.now",
                        "",
                        out,
                        null,
                        start,
                        java.time.Duration.between(start, Instant.now()).toMillis()
                ));
            }
            return out;
        } catch (Exception e) {
            var ctx = TraceContextHolder.get();
            if (ctx != null) {
                ctx.addStep(new StepRecord(
                        ctx.steps().size() + 1,
                        "TOOL",
                        "time.now",
                        "",
                        null,
                        e.getClass().getSimpleName() + ": " + e.getMessage(),
                        start,
                        java.time.Duration.between(start, Instant.now()).toMillis()
                ));
            }
            throw e;
        }
    }

}