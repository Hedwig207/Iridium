package com.owlbyte.iridium.optimization;

import com.owlbyte.iridium.client.PerformanceProfiler;
import com.owlbyte.iridium.IridiumMod;
import com.owlbyte.iridium.config.IridiumConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Bootstraps the runtime optimization subsystems and reports the active profile.
 *
 * <p>Most optimizations live in Mixins ({@code mixin/}); this class only wires the few hooks that
 * need a live server event and emits a single startup diagnostic so server owners can verify which
 * features are actually enabled.</p>
 */
public final class IridiumOptimizations {

    private static final int REPORT_INTERVAL_TICKS = 600;

    private IridiumOptimizations() {
    }

    public static void init() {
        IridiumConfig cfg = IridiumConfig.get();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (cfg.blockEntityTickCulling()) {
                IridiumMod.LOGGER.info("[Iridium] Block entity tick culling active (range = {} blocks)",
                        cfg.blockEntityTickRange);
            } else {
                IridiumMod.LOGGER.info("[Iridium] Block entity tick culling disabled (vanilla behavior)");
            }
            if (cfg.randomTickCulling()) {
                IridiumMod.LOGGER.info("[Iridium] Random-tick chunk culling active (range = {} chunks)",
                        cfg.randomTickChunkRange);
            } else {
                IridiumMod.LOGGER.info("[Iridium] Random-tick chunk culling disabled (vanilla behavior)");
            }
            if (cfg.performanceProfiling) {
                IridiumMod.LOGGER.info("[Iridium] Performance profiling active (reports every {} seconds)",
                        REPORT_INTERVAL_TICKS / 20);
            } else {
                IridiumMod.LOGGER.info("[Iridium] Performance profiling disabled");
            }
        });

        if (cfg.performanceProfiling) {
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                if (server.getTicks() > 0 && server.getTicks() % REPORT_INTERVAL_TICKS == 0) {
                    PerformanceProfiler.report();
                }
            });
        }

        StringBuilder sb = new StringBuilder("[Iridium] Optimization profile: ");
        sb.append("server=").append(cfg.logicOptimizations)
                .append(" (blockEntityTickRange=").append(cfg.blockEntityTickRange)
                .append(", randomTickChunkRange=").append(cfg.randomTickChunkRange).append("), ");
        sb.append("client=").append(cfg.clientOptimizations)
                .append(" (blockEntityRenderDistance=").append(cfg.blockEntityRenderDistance)
                .append(", entityRenderDistance=").append(cfg.entityRenderDistance).append(')');
        IridiumMod.LOGGER.info(sb.toString());
    }
}