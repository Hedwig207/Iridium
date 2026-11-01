package com.owlbyte.iridium.logic;

import com.owlbyte.iridium.config.IridiumConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Decides whether a block entity is close enough to a player to keep ticking.
 *
 * <p>Every tick, {@code World.tickBlockEntities()} walks the entire list of pending block entity
 * tickers, including machines and farms in chunks nobody is near. On large worlds this dominates
 * the tick budget. When {@link IridiumConfig#blockEntityTickRange} is set, block entities farther
 * than that radius from every player are skipped.</p>
 *
 * <p><b>Safety:</b> the optimization is off by default ({@code range == 0}), only applies to loaded
 * server worlds, and never skips anything when no player is online. Disable it if you rely on
 * machines that must run while nobody is nearby.</p>
 */
public final class BlockEntityCuller {

    private BlockEntityCuller() {
    }

    /** @return {@code true} if the block entity at {@code pos} should be ticked this tick. */
    public static boolean shouldTick(World world, BlockPos pos) {
        IridiumConfig cfg = IridiumConfig.get();
        if (!cfg.blockEntityTickCulling() || !(world instanceof ServerWorld serverWorld)) {
            return true;
        }

        List<? extends PlayerEntity> players = serverWorld.getPlayers();
        if (players.isEmpty()) {
            return true;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        long rangeSq = (long) cfg.blockEntityTickRange * cfg.blockEntityTickRange;

        for (PlayerEntity player : players) {
            if (player.squaredDistanceTo(centerX, centerY, centerZ) <= rangeSq) {
                return true;
            }
        }
        return false;
    }
}