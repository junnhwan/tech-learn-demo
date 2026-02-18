package io.wanjune.agent.service;

import io.wanjune.agent.common.StepRecord;
import io.wanjune.agent.common.TraceContext;
import io.wanjune.agent.common.TraceContextHolder;
import io.wanjune.agent.config.AgentProperties;
import io.wanjune.agent.config.Assistant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author zjh
 * @since 2026/2/18 15:18
 */
@Service
public class AgentService {

    private final Assistant assistant;
    private final AgentProperties agentProperties;


    public AgentService(Assistant assistant, AgentProperties agentProperties) {
        this.assistant = assistant;
        this.agentProperties = agentProperties;
    }

    public AgentResult run(String query) {
        // 预算控制 与 可观测 的底座：traceId + steps
        String traceId = UUID.randomUUID().toString();
        TraceContext ctx = new TraceContext(traceId);
        TraceContextHolder.set(ctx);


        try {
            String answer = assistant.chat(query);
            return new AgentResult(traceId, answer, ctx.steps());
        } finally {
            TraceContextHolder.clear();
        }

    }

    public record AgentResult(String traceId, String answer, List<StepRecord> steps){}
}