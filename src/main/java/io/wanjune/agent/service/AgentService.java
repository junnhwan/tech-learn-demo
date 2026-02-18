package io.wanjune.agent.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import io.wanjune.agent.common.StepRecord;
import io.wanjune.agent.common.TraceContext;
import io.wanjune.agent.common.TraceContextHolder;
import io.wanjune.agent.config.properties.AgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 手动实现 ReAct 循环的 Agent 服务.
 * <p>
 * Agent 的本质就是一个 for 循环:
 * <pre>
 * for (step = 0; step < maxSteps; step++) {
 *     response = LLM.generate(messages, tools)
 *     if (没有工具调用) return 最终答案
 *     执行工具 → 结果追加到 messages
 * }
 * // 走到这里 = 预算耗尽
 * </pre>
 *
 * @author zjh
 * @since 2026/2/18 15:18
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private static final String SYSTEM_PROMPT = """
            You are an agent in a backend service.
            Use tools when needed.
            Keep answers concise.
            """;

    private final ChatLanguageModel chatModel;
    private final AgentProperties agentProperties;

    /** 注册过的工具规格（告诉 LLM 有哪些工具可用） */
    private final List<ToolSpecification> toolSpecs = new ArrayList<>();
    /** 工具名 → 执行器，用于 dispatch 工具调用 */
    private final Map<String, ToolExecutor> toolExecutors = new HashMap<>();

    public AgentService(ChatLanguageModel chatModel,
                        AgentProperties agentProperties,
                        List<Object> tools) {
        this.chatModel = chatModel;
        this.agentProperties = agentProperties;
        tools.forEach(this::registerTool);
        log.info("AgentService initialized with {} tool(s): {}", toolSpecs.size(),
                toolExecutors.keySet());
    }

    // ─────────────────── ReAct 循环 ───────────────────

    public AgentResult run(String query) {
        String traceId = UUID.randomUUID().toString();
        TraceContext ctx = new TraceContext(traceId);
        TraceContextHolder.set(ctx);

        int maxSteps = agentProperties.maxSteps() != null ? agentProperties.maxSteps() : 5;

        // 消息列表 = Agent 的"工作记忆"
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(SYSTEM_PROMPT));
        messages.add(UserMessage.from(query));

        try {
            for (int step = 0; step < maxSteps; step++) {
                // ① 把当前消息列表 + 工具列表交给 LLM
                Response<AiMessage> response = chatModel.generate(messages, toolSpecs);
                AiMessage aiMessage = response.content();
                messages.add(aiMessage);

                // ② 如果 LLM 没有请求调工具 → 它给出了最终答案，循环结束
                if (!aiMessage.hasToolExecutionRequests()) {
                    return new AgentResult(traceId, aiMessage.text(), ctx.steps(), false);
                }

                // ③ 逐个执行 LLM 请求的工具调用
                for (ToolExecutionRequest req : aiMessage.toolExecutionRequests()) {
                    Instant start = Instant.now();
                    String result;
                    String error = null;

                    try {
                        ToolExecutor executor = toolExecutors.get(req.name());
                        if (executor == null) {
                            throw new IllegalArgumentException("Unknown tool: " + req.name());
                        }
                        result = executor.execute(req, null);
                    } catch (Exception e) {
                        result = "Error: " + e.getMessage();
                        error = e.getClass().getSimpleName() + ": " + e.getMessage();
                    }

                    // 把工具结果追加到消息列表，下一轮 LLM 就能看到
                    messages.add(ToolExecutionResultMessage.from(req, result));

                    // 记录到 trace（可观测性）
                    ctx.addStep(new StepRecord(
                            ctx.steps().size() + 1,
                            "TOOL",
                            req.name(),
                            req.arguments(),
                            result,
                            error,
                            start,
                            Duration.between(start, Instant.now()).toMillis()
                    ));
                }
                // ④ 回到循环顶部，带着工具结果再问 LLM
            }

            // ⑤ 走到这里说明循环了 maxSteps 轮，预算耗尽
            log.warn("Budget exhausted for trace={}, maxSteps={}", traceId, maxSteps);
            return new AgentResult(traceId,
                    "[Budget exhausted: reached max " + maxSteps + " steps]",
                    ctx.steps(), true);

        } finally {
            TraceContextHolder.clear();
        }
    }

    // ─────────────────── 工具注册 ───────────────────

    private void registerTool(Object toolBean) {
        List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(toolBean);
        toolSpecs.addAll(specs);

        for (Method method : toolBean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                // 找到同名的 spec，用它的 name 作为 key
                String toolName = specs.stream()
                        .filter(s -> s.name().equals(method.getName()))
                        .map(ToolSpecification::name)
                        .findFirst()
                        .orElse(method.getName());
                toolExecutors.put(toolName, new DefaultToolExecutor(toolBean, method));
            }
        }
    }

    // ─────────────────── 结果 ───────────────────

    public record AgentResult(
            String traceId,
            String answer,
            List<StepRecord> steps,
            boolean budgetExhausted
    ) {}
}
