package com.owlbyte.iridium.client;

import com.owlbyte.iridium.config.IridiumConfig;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * Counts real, observable units of work that Iridium skipped (culled), so server owners and
 * players can verify the mod is doing something rather than trusting an unquantified "feels
 * smoother" impression.
 *
 * <p>Counters are {@link LongAdder}s because they may be touched from several threads (render
 * thread, server thread) every tick; the update cost is a couple of nanoseconds and never blocks
 * the hot path.</p>
 */
public final class PerformanceProfiler {

    private static final boolean ENABLED = IridiumConfig.get().performanceProfiling;

    private static final LongAdder CULLED_BLOCK_ENTITY_TICKS = new LongAdder();
    private static final LongAdder CULLED_CHUNK_TICKS = new LongAdder();
    private static final LongAdder CULLED_BLOCK_ENTITY_RENDERS = new LongAdder();
    private static final LongAdder CULLED_ENTITY_RENDERS = new LongAdder();

    private PerformanceProfiler() {
    }

    /** Records that a server-side block entity tick was skipped. */
    public static void culledBlockEntityTick() {
        if (ENABLED) {
            CULLED_BLOCK_ENTITY_TICKS.increment();
        }
    }

    /** Records that a per-chunk tick pass was skipped. */
    public static void culledChunkTick() {
        if (ENABLED) {
            CULLED_CHUNK_TICKS.increment();
        }
    }

    /** Records that a client-side block entity render was skipped. */
    public static void culledBlockEntityRender() {
        if (ENABLED) {
            CULLED_BLOCK_ENTITY_RENDERS.increment();
        }
    }

    /** Records that a client-side entity render was skipped. */
    public static void culledEntityRender() {
        if (ENABLED) {
            CULLED_ENTITY_RENDERS.increment();
        }
    }

    /**
     * Emits a human-readable report of the counters collected since the last report, resets them,
     * and returns the combined count of everything skipped (0 if profiling is disabled).
     */
    public static long report() {
        if (!ENABLED) {
            return 0;
        }
        long bet = CULLED_BLOCK_ENTITY_TICKS.sumThenReset();
        long ct = CULLED_CHUNK_TICKS.sumThenReset();
        long ber = CULLED_BLOCK_ENTITY_RENDERS.sumThenReset();
        long er = CULLED_ENTITY_RENDERS.sumThenReset();
        long total = bet + ct + ber + er;
        if (total == 0) {
            return 0;
        }

        StringBuilder sb = new StringBuilder("[Iridium] [perf] skipped: ");
        if (er > 0) {
            sb.append("entities=").append(er).append(' ');
        }
        if (ber > 0) {
            sb.append("blockEntityRenders=").append(ber).append(' ');
        }
        if (bet > 0) {
            sb.append("blockEntityTicks=").append(bet).append(' ');
        }
        if (ct > 0) {
            sb.append("chunkTicks=").append(ct).append(' ');
        }
        LoggerFactory.getLogger("Iridium").info(sb.toString().trim());
        return total;
    }
}