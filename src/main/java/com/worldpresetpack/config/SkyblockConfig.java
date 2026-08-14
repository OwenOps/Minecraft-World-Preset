package com.worldpresetpack.config;

/**
 * Configuration en mémoire pour la création d'un monde Skyblock.
 * Les valeurs sont réinitialisées à chaque session (pas de persistance fichier).
 */
public final class SkyblockConfig {

    // Empêcher l'instanciation
    private SkyblockConfig() {}

    public enum BiomeMode {
        /** Utilise uniquement le biome minecraft:the_void */
        VOID,
        /** Utilise les biomes standard du générateur vanilla */
        STANDARD
    }

    /** Génération des structures dans l'Overworld */
    public static boolean generateStructures = true;

    /** Mode biome pour l'Overworld */
    public static BiomeMode biomeMode = BiomeMode.VOID;

    /** Génération des structures dans le Nether */
    public static boolean netherStructures = false;

    /** Remet toutes les options à leurs valeurs par défaut. */
    public static void reset() {
        generateStructures = true;
        biomeMode = BiomeMode.VOID;
        netherStructures = false;
    }
}
