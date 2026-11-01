package com.owlbyte.iridium.client;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/**
 * Shared distance checks for client-side render culling.
 *
 * <p>Both block entity and entity render paths use these helpers. All checks are squared-distance
 * comparisons so no square root is needed on the render thread.</p>
 */
public final class RenderDistance {

    private RenderDistance() {
    }

    /**
     * @return {@code true} if {@code (x,y,z)} is farther than {@code maxDistance} blocks from the
     *     (center of) the camera view.
     */
    public static boolean tooFar(double x, double y, double z, Camera camera, double maxDistance) {
        if (camera == null) {
            return false;
        }
        Vec3d pos = camera.getPos();
        double dx = x - pos.x;
        double dy = y - pos.y;
        double dz = z - pos.z;
        double maxSq = maxDistance * maxDistance;
        return dx * dx + dy * dy + dz * dz > maxSq;
    }

    /** Center of a block cell as absolute coordinates. */
    public static double center(int coordinate) {
        return coordinate + 0.5D;
    }
}