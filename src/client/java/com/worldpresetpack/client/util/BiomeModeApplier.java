package com.worldpresetpack.client.util;

import com.worldpresetpack.config.SkyblockConfig;
import com.worldpresetpack.registry.ModWorldPresets;
import com.worldpresetpack.worldgen.VoidChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

import java.util.HashMap;
import java.util.Map;

public final class BiomeModeApplier {

    private BiomeModeApplier() {}

    public static void apply(WorldCreationUiState uiState, SkyblockConfig.BiomeMode mode) {
        boolean isSkyblock = uiState.getWorldType().preset().is(ModWorldPresets.SKYBLOCK);
        if (!isSkyblock) return;

        uiState.updateDimensions((registryAccess, currentDims) -> {
            HolderGetter<NoiseGeneratorSettings> noiseGetter = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
            Holder<NoiseGeneratorSettings> overworldNoise = noiseGetter.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            Holder<NoiseGeneratorSettings> netherNoise = noiseGetter.getOrThrow(NoiseGeneratorSettings.NETHER);

            HolderGetter<MultiNoiseBiomeSourceParameterList> paramGetter =
                    registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

            boolean structs = SkyblockConfig.generateStructures;

            net.minecraft.world.level.chunk.ChunkGenerator overworldGen;
            if (mode == SkyblockConfig.BiomeMode.STANDARD) {
                Holder<MultiNoiseBiomeSourceParameterList> overworldParams =
                        paramGetter.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
                overworldGen = new VoidChunkGenerator(
                        MultiNoiseBiomeSource.createFromPreset(overworldParams), overworldNoise, structs);
            } else {
                HolderGetter<Biome> biomeGetter = registryAccess.lookupOrThrow(Registries.BIOME);
                Holder<Biome> voidBiome = biomeGetter.getOrThrow(Biomes.THE_VOID);
                overworldGen = new VoidChunkGenerator(new FixedBiomeSource(voidBiome), overworldNoise, structs);
            }

            Holder<MultiNoiseBiomeSourceParameterList> netherParams =
                    paramGetter.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
            net.minecraft.world.level.chunk.ChunkGenerator netherGen = new VoidChunkGenerator(
                    MultiNoiseBiomeSource.createFromPreset(netherParams), netherNoise, structs);

            WorldDimensions rebuilt = currentDims.replaceOverworldGenerator(registryAccess, overworldGen);

            Map<ResourceKey<LevelStem>, LevelStem> newDims = new HashMap<>(rebuilt.dimensions());
            LevelStem existingNether = newDims.get(LevelStem.NETHER);
            if (existingNether != null) {
                newDims.put(LevelStem.NETHER, new LevelStem(existingNether.type(), netherGen));
            }
            return new WorldDimensions(newDims);
        });
    }
}
