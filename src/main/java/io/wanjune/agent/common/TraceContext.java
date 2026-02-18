package io.wanjune.agent.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zjh
 * @since 2026/2/18 13:44
 */
public class TraceContext {

    private final String traceId;
    private final Instant startedAt = Instant.now();
    private final List<StepRecord> steps = new ArrayList<>();
    public TraceContext(String traceId) { this.traceId = traceId; }
    public String traceId() { return traceId; }
    public Instant startedAt() { return startedAt; }
    public List<StepRecord> steps() { return steps; }
    public void addStep(StepRecord step) { steps.add(step); }

}