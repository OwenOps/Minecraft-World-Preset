package com.worldpresetpack.config;

/**
 * In-memory config for Skyblock world creation.
 * Reset each session (not persisted to disk).
 */
public final class SkyblockConfig {

    private SkyblockConfig() {}

    public enum BiomeMode {
        /** Using all the biomes */
        STANDARD,

        /** Using only minecraft:the_void */
        VOID,
    }

    /** Structures in Overworld and Nether. Off by default, like classic Skyblock. */
    public static boolean generateStructures = false;

    /** Overworld biome mode. */
    public static BiomeMode biomeMode = BiomeMode.STANDARD;

    /** Restore every option to its default. */
    public static void reset() {
        generateStructures = false;
        biomeMode = BiomeMode.STANDARD;
    }
}
