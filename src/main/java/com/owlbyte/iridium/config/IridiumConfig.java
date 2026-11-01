package com.owlbyte.iridium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.owlbyte.iridium.IridiumMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Central configuration registry for Iridium.
 *
 * <p>Loaded from {@code config/iridium.json}. Every optimization reads its toggles from here so any
 * single feature can be switched off independently to restore exact vanilla behavior.</p>
 */
public final class IridiumConfig {

    /* ---- Master toggles ---- */
    /** Server-side tick-logic optimizations. */
    public boolean logicOptimizations = true;
    /** Client-side rendering optimizations. */
    public boolean clientOptimizations = true;

    /* ---- Server: tick logic ---- */
    /**
     * Radius (in blocks) around the nearest player within which block entities keep ticking.
     * {@code 0} disables the optimization entirely (vanilla behavior). Values above zero mean
     * block entities farther than this from every player are skipped, which is the single largest
     * saving on worlds with large offline machine/farm areas.
     */
    public int blockEntityTickRange = 0;

    /**
     * Radius (in chunks) around the nearest player within which random ticks are processed for a
     * chunk. {@code 0} disables the optimization. Farther chunks skip their random-tick pass
     * entirely (crops stop growing, fire stops spreading, etc.) but otherwise behave normally.
     * Defaults to {@code 0} so it never alters gameplay without an explicit choice.
     */
    public int randomTickChunkRange = 0;

    /* ---- Client: rendering ---- */
    /**
     * Distance (in blocks) beyond which block entity renderers are skipped.
     * {@code 0} disables the optimization. Purely visual, never affects gameplay logic.
     */
    public int blockEntityRenderDistance = 96;

    /**
     * Distance (in blocks) beyond which entity renderers are skipped. {@code 0} disables it.
     * Items, mobs, boats and vehicles are the most expensive renderers on the client, and vanilla
     * only frustum-culls them (no distance cull). Purely visual; never affects gameplay.
     */
    public int entityRenderDistance = 128;

    /**
     * If {@code true}, Iridium counts how much work each cull actually skipped and prints a
     * periodic performance report. Pure bookkeeping; never alters behavior.
     */
    public boolean performanceProfiling = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static IridiumConfig instance;

    private IridiumConfig() {
    }

    public static IridiumConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static Path getPath() {
        return Path.of("config", "iridium.json");
    }

    public static void load() {
        instance = new IridiumConfig();
        Path path = getPath();
        if (!Files.exists(path)) {
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(path), JsonObject.class);
            if (root != null) {
                instance.apply(root);
            }
        } catch (Exception e) {
            IridiumMod.LOGGER.error("[Iridium] Failed to read config, using defaults.", e);
            instance = new IridiumConfig();
        }
    }

    public static void write() {
        try {
            Path path = getPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(instance));
        } catch (IOException e) {
            IridiumMod.LOGGER.error("[Iridium] Failed to write config.", e);
        }
    }

    /** Applies known fields from a root JSON object (also accepts a nested "iridium" block). */
    private void apply(JsonObject root) {
        JsonObject src = root;
        if (root.has("iridium") && root.get("iridium").isJsonObject()) {
            src = root.getAsJsonObject("iridium");
        }
        if (src.has("logicOptimizations")) {
            logicOptimizations = src.get("logicOptimizations").getAsBoolean();
        }
        if (src.has("clientOptimizations")) {
            clientOptimizations = src.get("clientOptimizations").getAsBoolean();
        }
        if (src.has("blockEntityTickRange")) {
            blockEntityTickRange = Math.max(0, src.get("blockEntityTickRange").getAsInt());
        }
        if (src.has("blockEntityRenderDistance")) {
            blockEntityRenderDistance = Math.max(0, src.get("blockEntityRenderDistance").getAsInt());
        }
        if (src.has("randomTickChunkRange")) {
            randomTickChunkRange = Math.max(0, src.get("randomTickChunkRange").getAsInt());
        }
        if (src.has("entityRenderDistance")) {
            entityRenderDistance = Math.max(0, src.get("entityRenderDistance").getAsInt());
        }
        if (src.has("performanceProfiling")) {
            performanceProfiling = src.get("performanceProfiling").getAsBoolean();
        }
    }

    /** Whether server-side block entity tick culling is active. */
    public boolean blockEntityTickCulling() {
        return logicOptimizations && blockEntityTickRange > 0;
    }

    /** Whether server-side random-tick chunk culling is active. */
    public boolean randomTickCulling() {
        return logicOptimizations && randomTickChunkRange > 0;
    }

    /** Whether client-side block entity render culling is active. */
    public boolean blockEntityRenderCulling() {
        return clientOptimizations && blockEntityRenderDistance > 0;
    }

    /** Whether client-side entity render culling is active. */
    public boolean entityRenderCulling() {
        return clientOptimizations && entityRenderDistance > 0;
    }
}