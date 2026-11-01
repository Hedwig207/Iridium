package com.owlbyte.iridium;

import com.owlbyte.iridium.config.IridiumConfig;
import com.owlbyte.iridium.optimization.IridiumOptimizations;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IridiumMod implements ModInitializer {

    public static final String MOD_ID = "iridium";
    public static final Logger LOGGER = LoggerFactory.getLogger("Iridium");

    @Override
    public void onInitialize() {
        LOGGER.info("[Iridium] Loading configuration...");
        IridiumConfig.load();
        IridiumConfig.write();

        LOGGER.info("[Iridium] Registering optimization systems...");
        IridiumOptimizations.init();

        IridiumConfig cfg = IridiumConfig.get();
        LOGGER.info("[Iridium] Iridium initialized. Server tick optimizations: {}, client render optimizations: {}",
                cfg.logicOptimizations, cfg.clientOptimizations);
    }
}