package com.owlbyte.iridium.logic;

import com.owlbyte.iridium.mem.BlockPosPool;
import net.minecraft.util.math.BlockPos;

/**
 * Allocation optimization for the scheduled block-tick path ({@code ServerWorld.tickBlock}).
 *
 * <p>Every scheduled block tick (redstone updates, random ticks, fluid ticks) is invoked with a
 * freshly created immutable {@link BlockPos}. On a busy server this is one of the most frequent
 * short-lived allocations in the game loop and contributes measurable young-generation pressure.
 * When enabled, this optimizer swaps the incoming position for a recycled {@link BlockPos.Mutable}
 * obtained from {@link BlockPosPool}, then returns it once the tick has finished.</p>
 *
 * <p><b>Behavioral safety:</b> only the identity of the position object changes, never the
 * coordinates or the order/rate of ticks. The pooled instance lives strictly for the duration of a
 * single {@code tickBlock} call, so vanilla mechanics are preserved. The whole optimization can be
 * disabled from {@code config/iridium.json} for maximum compatibility.</p>
 */
public final class BlockTickOptimizer {

    private BlockTickOptimizer() {
    }

    /** Always {@code false}: position pooling is kept for reference only and is no longer registered. */
    public static boolean poolingEnabled() {
        return false;
    }

    /** Acquire a pooled mutable copy of {@code pos} for use during a single block tick. */
    public static BlockPos.Mutable acquire(BlockPos pos) {
        return BlockPosPool.obtain(pos);
    }

    /** Return a position previously acquired via {@link #acquire} back to the pool. */
    public static void release(BlockPos.Mutable pooled) {
        BlockPosPool.release(pooled);
    }
}