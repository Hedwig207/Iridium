package com.owlbyte.iridium.mixin.client;

import com.owlbyte.iridium.client.PerformanceProfiler;
import com.owlbyte.iridium.client.RenderDistance;
import com.owlbyte.iridium.config.IridiumConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips entity rendering for entities farther than a configurable distance from the camera.
 *
 * <p>Vanilla frustum-culls entities but does no distance culling: mobs, item entities, boats and
 * other vehicles far outside the view are still submitted and their (expensive) renderers run even
 * when the player cannot realistically see them. This is the single most costly non-block workflow
 * the client runs, so culling it usually frees more frames than block entity culling. Purely
 * visual and server-independent.</p>
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void iridium$cullFarEntity(Entity entity, double x, double y, double z,
                                       float entityYaw, float tickDelta,
                                       MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                       int light, CallbackInfo ci) {
        IridiumConfig cfg = IridiumConfig.get();
        if (!cfg.entityRenderCulling()) {
            return;
        }

        Camera camera = ((EntityRenderDispatcher) (Object) this).camera;
        if (RenderDistance.tooFar(x, y, z, camera, cfg.entityRenderDistance)) {
            PerformanceProfiler.culledEntityRender();
            ci.cancel();
        }
    }
}