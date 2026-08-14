package com.worldpresetpack.registry;

import com.worldpresetpack.WorldPresetPackMod;
import com.worldpresetpack.worldgen.VoidChunkGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public final class ModWorldPresets {

    private ModWorldPresets() {}

    /** Clé ResourceKey du preset "skyblock" dans le registre WorldPreset. */
    public static final ResourceKey<WorldPreset> SKYBLOCK = ResourceKey.create(
            Registries.WORLD_PRESET,
            Identifier.fromNamespaceAndPath("worldpresetpack", "skyblock")
    );

    /**
     * Enregistre le Codec de VoidChunkGenerator dans le registre des ChunkGenerators.
     * Doit être appelé depuis {@code WorldPresetPackMod.onInitialize()}.
     */
    public static void register() {
        Registry.register(
                BuiltInRegistries.CHUNK_GENERATOR,
                Identifier.fromNamespaceAndPath("worldpresetpack", "void"),
                VoidChunkGenerator.CODEC
        );
        WorldPresetPackMod.LOGGER.info("[WorldPresetPack] VoidChunkGenerator registered (id: worldpresetpack:void)");
        WorldPresetPackMod.LOGGER.info("[WorldPresetPack] Skyblock world preset key registered: {}", SKYBLOCK.identifier());
    }
}
