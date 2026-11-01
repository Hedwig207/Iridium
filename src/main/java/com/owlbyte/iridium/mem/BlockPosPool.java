package com.owlbyte.iridium.mem;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A small, thread-confined pool that recycles mutable block-position objects during hot
 * chunk-tick loops.
 *
 * <p>Minecraft allocates many short-lived {@link BlockPos} instances per tick (block lookups,
 * scheduled ticks, state checks). While the young-generation collector handles most of them,
 * pooling the mutable variant inside a single logical task noticeably reduces churn in
 * large-server scenarios.</p>
 *
 * <p><b>Contract:</b> callers must not retain a pooled {@link BlockPos.Mutable} beyond the
 * enclosing task, and must always return it via {@link #release}. Never store it in fields.</p>
 */
public final class BlockPosPool {

    private static final int MAX_POOLED = 8192;
    private static final ThreadLocal<Deque<BlockPos.Mutable>> POOL = ThreadLocal.withInitial(ArrayDeque::new);

    private BlockPosPool() {
    }

    /** Acquire a mutable copy of {@code src}. Caller must eventually {@link #release} it. */
    public static BlockPos.Mutable obtain(BlockPos src) {
        Deque<BlockPos.Mutable> pool = POOL.get();
        BlockPos.Mutable m = pool.poll();
        if (m == null) {
            m = new BlockPos.Mutable();
        }
        return m.set(src);
    }

    /** Acquire a mutable positioned at the given coordinates. */
    public static BlockPos.Mutable obtain(int x, int y, int z) {
        Deque<BlockPos.Mutable> pool = POOL.get();
        BlockPos.Mutable m = pool.poll();
        if (m == null) {
            m = new BlockPos.Mutable();
        }
        return m.set(x, y, z);
    }

    /** Return a previously acquired mutable to the pool. */
    public static void release(BlockPos.Mutable pos) {
        if (pos == null) {
            return;
        }
        Deque<BlockPos.Mutable> pool = POOL.get();
        if (pool.size() < MAX_POOLED) {
            pool.push(pos);
        }
    }
}