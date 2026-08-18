package com.worldpresetpack.worldgen;

import com.worldpresetpack.config.SkyblockConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds void Overworld + Nether stems. Minecraft-only so a Forge client can reuse it.
 */
public final class VoidDimensions {

    private VoidDimensions() {}

    public static WorldDimensions withVoidOverworldAndNether(
            RegistryAccess registryAccess,
            WorldDimensions current,
            BiomeSource overworldBiomes,
            boolean structures,
            SkyblockConfig.Difficulty difficulty,
            VoidChunkGenerator.Kind kind) {
        HolderGetter<NoiseGeneratorSettings> noise = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
        ChunkGenerator overworld = new VoidChunkGenerator(
                overworldBiomes,
                noise.getOrThrow(NoiseGeneratorSettings.OVERWORLD),
                structures,
                difficulty,
                kind);
        ChunkGenerator nether = new VoidChunkGenerator(
                netherBiomes(registryAccess),
                noise.getOrThrow(NoiseGeneratorSettings.NETHER),
                structures,
                difficulty,
                kind);
        return replaceOverworldAndNether(registryAccess, current, overworld, nether);
    }

    public static BiomeSource netherBiomes(RegistryAccess registryAccess) {
        HolderGetter<MultiNoiseBiomeSourceParameterList> params =
                registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        Holder<MultiNoiseBiomeSourceParameterList> nether =
                params.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
        return MultiNoiseBiomeSource.createFromPreset(nether);
    }

    public static WorldDimensions replaceOverworldAndNether(
            RegistryAccess registryAccess,
            WorldDimensions current,
            ChunkGenerator overworld,
            ChunkGenerator nether) {
        WorldDimensions rebuilt = current.replaceOverworldGenerator(registryAccess, overworld);
        Map<ResourceKey<LevelStem>, LevelStem> newDims = new HashMap<>(rebuilt.dimensions());
        LevelStem existingNether = newDims.get(LevelStem.NETHER);
        if (existingNether != null) {
            newDims.put(LevelStem.NETHER, new LevelStem(existingNether.type(), nether));
        }
        return new WorldDimensions(newDims);
    }
}
