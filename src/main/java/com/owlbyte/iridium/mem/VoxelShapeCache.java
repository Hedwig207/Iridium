package com.owlbyte.iridium.mem;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * Cache for the most commonly re-created voxel shapes in the vanilla code path.
 *
 * <p>Several blocks allocate fresh {@link VoxelShape}s (full-cube, empty, or unit cube) on each
 * collision/shape query, even though the result is effectively a constant. This class hands out
 * canonical singletons so those allocations collapse to shared immutable instances.</p>
 */
public final class VoxelShapeCache {

    private static final VoxelShape FULL_CUBE = VoxelShapes.fullCube();
    private static final VoxelShape EMPTY = VoxelShapes.empty();

    private VoxelShapeCache() {
    }

    public static VoxelShape fullCube() {
        return FULL_CUBE;
    }

    public static VoxelShape empty() {
        return EMPTY;
    }

    /** Called when a world unloads; shapes are static and safe to re-use across worlds. */
    public static void clear() {
        // Intentionally a no-op that documents intent: the singletons above are immutable and
        // shared across worlds. Kept as a hook for future dynamic caches.
    }
}