package io.wanjune.agent.common;

/**
 * @author zjh
 * @since 2026/2/18 13:42
 */
public class TraceContextHolder {
    private static final ThreadLocal<TraceContext> TL = new ThreadLocal<>();
    public static void set(TraceContext ctx) { TL.set(ctx); }
    public static TraceContext get() { return TL.get(); }
    public static void clear() { TL.remove(); }
}