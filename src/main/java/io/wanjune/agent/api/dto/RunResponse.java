package io.wanjune.agent.api.dto;

import io.wanjune.agent.common.StepRecord;

import java.util.List;

/**
 * @author zjh
 * @since 2026/2/18 14:40
 */
public record RunResponse(
        String traceId,
        String answer,
        List<StepRecord> steps,
        boolean budgetExhausted) {
}