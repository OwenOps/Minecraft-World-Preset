package com.worldpresetpack.config;

/**
 * Configuration en mémoire pour la création d'un monde Skyblock.
 * Les valeurs sont réinitialisées à chaque session (pas de persistance fichier).
 */
public final class SkyblockConfig {

    // Empêcher l'instanciation
    private SkyblockConfig() {}

    public enum BiomeMode {
        /** Using all the biomes */
        STANDARD,

        /** Using only minecraft:the_void */
        VOID,
    }

    /** Structures in Overworld and Nether. Off by default, like classic Skyblock. */
    public static boolean generateStructures = false;

    /** Mode biome pour l'Overworld */
    public static BiomeMode biomeMode = BiomeMode.STANDARD;

    /** Remet toutes les options à leurs valeurs par défaut. */
    public static void reset() {
        generateStructures = false;
        biomeMode = BiomeMode.STANDARD;
    }
}
