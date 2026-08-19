package com.worldpresetpack.config;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * In-memory config for Skyblock world creation.
 * Reset each session (not persisted to disk). Difficulty is also written into
 * {@code VoidChunkGenerator} so the island can read it when the world first ticks.
 * Overworld biome is applied as the dimension biome source (not a generator field).
 */
public final class SkyblockConfig {

    private SkyblockConfig() {}

    /**
     * Overworld climate. {@link #VANILLA} is multi-noise (spawn follows the seed);
     * {@link #VOID} is The Void everywhere; every other value forces that biome
     * around spawn, then vanilla mix beyond.
     */
    public enum OverworldBiome implements StringRepresentable {
        VANILLA("vanilla", null),
        VOID("void", Biomes.THE_VOID),
        PLAINS("plains", Biomes.PLAINS),
        SNOWY_PLAINS("snowy_plains", Biomes.SNOWY_PLAINS),
        ICE_SPIKES("ice_spikes", Biomes.ICE_SPIKES),
        DESERT("desert", Biomes.DESERT),
        SAVANNA("savanna", Biomes.SAVANNA),
        BADLANDS("badlands", Biomes.BADLANDS),
        FOREST("forest", Biomes.FOREST),
        FLOWER_FOREST("flower_forest", Biomes.FLOWER_FOREST),
        BIRCH_FOREST("birch_forest", Biomes.BIRCH_FOREST),
        DARK_FOREST("dark_forest", Biomes.DARK_FOREST),
        TAIGA("taiga", Biomes.TAIGA),
        SNOWY_TAIGA("snowy_taiga", Biomes.SNOWY_TAIGA),
        JUNGLE("jungle", Biomes.JUNGLE),
        CHERRY_GROVE("cherry_grove", Biomes.CHERRY_GROVE),
        PALE_GARDEN("pale_garden", Biomes.PALE_GARDEN),
        SWAMP("swamp", Biomes.SWAMP),
        MANGROVE_SWAMP("mangrove_swamp", Biomes.MANGROVE_SWAMP),
        MUSHROOM_FIELDS("mushroom_fields", Biomes.MUSHROOM_FIELDS),
        MEADOW("meadow", Biomes.MEADOW),
        OCEAN("ocean", Biomes.OCEAN),
        WARM_OCEAN("warm_ocean", Biomes.WARM_OCEAN),
        FROZEN_OCEAN("frozen_ocean", Biomes.FROZEN_OCEAN);

        public static final Codec<OverworldBiome> CODEC = StringRepresentable.fromEnum(OverworldBiome::values);

        private final String id;
        private final ResourceKey<Biome> fixedBiome;

        OverworldBiome(String id, ResourceKey<Biome> fixedBiome) {
            this.id = id;
            this.fixedBiome = fixedBiome;
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        /** {@code null} means vanilla multi-noise with no spawn override. */
        public ResourceKey<Biome> fixedBiome() {
            return fixedBiome;
        }

        public boolean allowsStructures() {
            return this != VOID;
        }
    }

    public enum Difficulty implements StringRepresentable {
        EASY("easy"),
        CLASSIC("classic"),
        HARD("hard");

        public static final Codec<Difficulty> CODEC = StringRepresentable.fromEnum(Difficulty::values);

        private final String id;

        Difficulty(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    /** Structures in Overworld and Nether. Off by default, like classic Skyblock. */
    public static boolean generateStructures = false;

    /** Overworld biomes. Vanilla mix by default. */
    public static OverworldBiome overworldBiome = OverworldBiome.VANILLA;

    /** Island size and starter chest. Classic matches the original island. */
    public static Difficulty difficulty = Difficulty.CLASSIC;

    /** Restore every option to its default. */
    public static void reset() {
        generateStructures = false;
        overworldBiome = OverworldBiome.VANILLA;
        difficulty = Difficulty.CLASSIC;
    }
}
