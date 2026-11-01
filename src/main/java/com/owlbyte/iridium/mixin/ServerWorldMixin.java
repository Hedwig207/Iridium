package com.owlbyte.iridium.mixin;

import com.owlbyte.iridium.logic.BlockTickOptimizer;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies Iridium's allocation optimization to the server-side scheduled block-tick path.
 *
 * <p>{@code tickBlock(BlockPos, Block)} is called once per scheduled block tick (redstone, random
 * ticks, fluids) and receives a freshly allocated immutable position each time. When pooling is
 * enabled we replace that argument with a recycled {@link BlockPos.Mutable} from the pool and return
 * it at the end of the call. The coordinates are identical and the tick order/rate are untouched,
 * so vanilla behavior is preserved; only the allocation pattern changes.</p>
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    /** The pooled position currently in use for this world's active {@code tickBlock} call, if any. */
    @Unique
    private BlockPos.Mutable iridium$pooledTickPos;

    @ModifyVariable(method = "tickBlock", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BlockPos iridium$poolTickPos(BlockPos pos) {
        if (!BlockTickOptimizer.poolingEnabled()) {
            this.iridium$pooledTickPos = null;
            return pos;
        }
        BlockPos.Mutable pooled = BlockTickOptimizer.acquire(pos);
        this.iridium$pooledTickPos = pooled;
        return pooled;
    }

    @Inject(method = "tickBlock", at = @At("TAIL"))
    private void iridium$releaseTickPos(BlockPos pos, Block block, CallbackInfo ci) {
        BlockPos.Mutable pooled = this.iridium$pooledTickPos;
        this.iridium$pooledTickPos = null;
        if (pooled != null) {
            BlockTickOptimizer.release(pooled);
        }
    }
}