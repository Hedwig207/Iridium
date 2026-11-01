package com.owlbyte.iridium.mixin.client;

import com.owlbyte.iridium.client.PerformanceProfiler;
import com.owlbyte.iridium.config.IridiumConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips block entity rendering for block entities beyond a configurable distance from the camera.
 *
 * <p>Vanilla still submits far block entity renderers (chests, signs, banners, beds, spawners, ...)
 * that pass the frustum test even when they are too far away to be noticeable. Culling them removes
 * both the renderer work and the vertex/lighting cost. This is purely visual and never touches
 * gameplay logic, so it is safe on both singleplayer and servers.</p>
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(
            method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void iridium$cullFarBlockEntity(BlockEntity blockEntity, float tickDelta,
                                            MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                            CallbackInfo ci) {
        IridiumConfig cfg = IridiumConfig.get();
        if (!cfg.blockEntityRenderCulling()) {
            return;
        }

        Camera camera = ((BlockEntityRenderDispatcher) (Object) this).camera;
        if (camera == null) {
            return;
        }

        Vec3d cameraPos = camera.getPos();
        BlockPos pos = blockEntity.getPos();
        double dx = cameraPos.x - (pos.getX() + 0.5D);
        double dy = cameraPos.y - (pos.getY() + 0.5D);
        double dz = cameraPos.z - (pos.getZ() + 0.5D);
        double maxDist = cfg.blockEntityRenderDistance;

        if (dx * dx + dy * dy + dz * dz > maxDist * maxDist) {
            PerformanceProfiler.culledBlockEntityRender();
            ci.cancel();
        }
    }
}