package io.wanjune.agent.common;

import java.time.Instant;

public record StepRecord(
        int index,
        String type,        // TOOL / INFO
        String name,        // tool name
        String input,
        String output,
        String error,
        Instant startedAt,
        long durationMs
) {}
