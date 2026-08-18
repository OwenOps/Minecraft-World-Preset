package com.worldpresetpack.registry;

import com.worldpresetpack.WorldPresetPackMod;
import com.worldpresetpack.worldgen.SkyblockBiomeSource;
import com.worldpresetpack.worldgen.VoidChunkGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public final class ModWorldPresets {

    private ModWorldPresets() {}

    public static final ResourceKey<WorldPreset> SKYBLOCK = ResourceKey.create(
            Registries.WORLD_PRESET,
            Identifier.fromNamespaceAndPath("worldpresetpack", "skyblock")
    );

    public static final ResourceKey<WorldPreset> ONE_BLOCK = ResourceKey.create(
            Registries.WORLD_PRESET,
            Identifier.fromNamespaceAndPath("worldpresetpack", "oneblock")
    );

    /**
     * Registers chunk-generator and biome-source codecs. Call from {@code WorldPresetPackMod.onInitialize()}.
     */
    public static void register() {
        Registry.register(
                BuiltInRegistries.CHUNK_GENERATOR,
                Identifier.fromNamespaceAndPath("worldpresetpack", "void"),
                VoidChunkGenerator.CODEC
        );
        Registry.register(
                BuiltInRegistries.BIOME_SOURCE,
                Identifier.fromNamespaceAndPath("worldpresetpack", "spawn_biome"),
                SkyblockBiomeSource.CODEC
        );
        WorldPresetPackMod.LOGGER.info("[WorldPresetPack] codecs registered (void, spawn_biome)");
    }
}
