package com.owlbyte.iridium.logic;

import com.owlbyte.iridium.config.IridiumConfig;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

/**
 * Decides whether a chunk should run its random-tick (and block entity tick) pass.
 *
 * <p>Vanilla runs the full block/random-tick pass for every loaded chunk every tick, even chunks
 * with no player anywhere near them. In worlds with wide lazily-loaded areas (farms spread across
 * many chunks, large cleared terrain) this wastes the tick budget. When
 * {@link IridiumConfig#randomTickChunkRange} is set, chunks farther than that radius from every
 * player skip the expensive pass entirely.</p>
 *
 * <p><b>Safety:</b> off by default ({@code 0}), only applies to {@link ServerWorld}, never skips
 * anything when no player is online, and only acts on chunk distance. This is coarser than the
 * per-block-entity {@link BlockEntityCuller}; both may run, and chunk-level culling simply
 * short-circuits before the finer block-level checks.</p>
 */
public final class ChunkActivityCuller {

    private ChunkActivityCuller() {
    }

    /** @return {@code true} if the chunk at {@code pos} should run its tick pass this tick. */
    public static boolean shouldTick(ServerWorld world, ChunkPos pos) {
        IridiumConfig cfg = IridiumConfig.get();
        if (!cfg.randomTickCulling()) {
            return true;
        }

        var players = world.getPlayers();
        if (players.isEmpty()) {
            return true;
        }

        // Distance in chunk units from chunk center to player.
        double cx = pos.getCenterX();
        double cz = pos.getCenterZ();
        long rangeSq = Math.max(1L, (long) cfg.randomTickChunkRange * cfg.randomTickChunkRange);

        for (var player : players) {
            double dx = player.getX() - cx;
            double dz = player.getZ() - cz;
            if (dx * dx + dz * dz <= rangeSq) {
                return true;
            }
        }
        return false;
    }
}