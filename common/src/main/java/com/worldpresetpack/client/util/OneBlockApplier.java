package com.worldpresetpack.client.util;

import com.worldpresetpack.config.SkyblockConfig;
import com.worldpresetpack.registry.ModWorldPresets;
import com.worldpresetpack.worldgen.VoidChunkGenerator;
import com.worldpresetpack.worldgen.VoidDimensions;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;

public final class OneBlockApplier {

    private OneBlockApplier() {}

    public static void apply(WorldCreationUiState uiState) {
        if (!uiState.getWorldType().preset().is(ModWorldPresets.ONE_BLOCK)) {
            return;
        }

        uiState.updateDimensions((registryAccess, currentDims) -> VoidDimensions.withVoidOverworldAndNether(
                registryAccess,
                currentDims,
                new FixedBiomeSource(registryAccess.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)),
                false,
                SkyblockConfig.Difficulty.CLASSIC,
                VoidChunkGenerator.Kind.ONE_BLOCK));
    }
}
