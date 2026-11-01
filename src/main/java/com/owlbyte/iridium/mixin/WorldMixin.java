package com.owlbyte.iridium.mixin;

import com.owlbyte.iridium.client.PerformanceProfiler;
import com.owlbyte.iridium.logic.BlockEntityCuller;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hooks the block entity tick loop so far-away block entities can be skipped.
 *
 * <p>{@code World.tickBlockEntities()} iterates every pending {@link BlockEntityTickInvoker} and
 * calls {@link BlockEntityTickInvoker#tick()}. We redirect that single call through
 * {@link BlockEntityCuller}, which keeps vanilla behavior unless the activation range is enabled.</p>
 */
@Mixin(World.class)
public abstract class WorldMixin {

    @Redirect(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"
            )
    )
    private void iridium$tickBlockEntity(BlockEntityTickInvoker invoker) {
        BlockPos pos = invoker.getPos();
        if (BlockEntityCuller.shouldTick((World) (Object) this, pos)) {
            invoker.tick();
        } else {
            PerformanceProfiler.culledBlockEntityTick();
        }
    }
}