package com.worldpresetpack.client.util;

import com.worldpresetpack.config.SkyblockConfig;
import com.worldpresetpack.registry.ModWorldPresets;
import com.worldpresetpack.worldgen.SkyblockBiomeSource;
import com.worldpresetpack.worldgen.VoidChunkGenerator;
import com.worldpresetpack.worldgen.VoidDimensions;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;

public final class BiomeModeApplier {

    private BiomeModeApplier() {}

    public static void apply(WorldCreationUiState uiState) {
        if (!uiState.getWorldType().preset().is(ModWorldPresets.SKYBLOCK)) {
            return;
        }

        uiState.updateDimensions((registryAccess, currentDims) -> VoidDimensions.withVoidOverworldAndNether(
                registryAccess,
                currentDims,
                overworldBiomeSource(registryAccess),
                SkyblockConfig.generateStructures,
                SkyblockConfig.difficulty,
                VoidChunkGenerator.Kind.SKYBLOCK));
    }

    private static BiomeSource overworldBiomeSource(net.minecraft.core.RegistryAccess registryAccess) {
        SkyblockConfig.OverworldBiome choice = SkyblockConfig.overworldBiome;
        HolderGetter<Biome> biomeGetter = registryAccess.lookupOrThrow(Registries.BIOME);
        if (choice == SkyblockConfig.OverworldBiome.VOID) {
            return new FixedBiomeSource(biomeGetter.getOrThrow(Biomes.THE_VOID));
        }

        HolderGetter<MultiNoiseBiomeSourceParameterList> paramGetter =
                registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        BiomeSource vanillaMix = MultiNoiseBiomeSource.createFromPreset(
                paramGetter.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD));
        ResourceKey<Biome> spawnBiome = choice.fixedBiome();
        if (spawnBiome == null) {
            return vanillaMix;
        }
        return new SkyblockBiomeSource(
                vanillaMix,
                biomeGetter.getOrThrow(spawnBiome),
                SkyblockBiomeSource.DEFAULT_RADIUS_BLOCKS);
    }
}
