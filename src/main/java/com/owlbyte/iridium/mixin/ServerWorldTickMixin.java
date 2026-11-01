package com.owlbyte.iridium.mixin;

import com.owlbyte.iridium.client.PerformanceProfiler;
import com.owlbyte.iridium.logic.ChunkActivityCuller;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the per-chunk tick pass so chunks with no nearby player can skip their random/block ticks.
 *
 * <p>{@code ServerWorld.tickChunk(WorldChunk, int)} runs a chunk's block tick pass (including
 * random ticks, fluid ticks and block entity ticks). For a chunk far from every player this work is
 * not observable, so we cancel the whole pass. This is a coarse, safe cut: it never runs when
 * {@code randomTickChunkRange == 0}, and never skips when a player is in range.</p>
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldTickMixin {

    @Inject(
            method = "tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void iridium$cullInactiveChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerWorld self = (ServerWorld) (Object) this;
        ChunkPos pos = chunk.getPos();
        if (!ChunkActivityCuller.shouldTick(self, pos)) {
            PerformanceProfiler.culledChunkTick();
            ci.cancel();
        }
    }
}