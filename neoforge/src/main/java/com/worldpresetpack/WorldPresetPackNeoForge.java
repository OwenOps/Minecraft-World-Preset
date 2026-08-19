package com.worldpresetpack;

import com.worldpresetpack.worldgen.SkyblockBiomeSource;
import com.worldpresetpack.worldgen.VoidChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * NeoForge entry. Gameplay lives in {@link WorldPresetPack} (Minecraft APIs only).
 */
@Mod(WorldPresetPack.MOD_ID)
public class WorldPresetPackNeoForge {

    public WorldPresetPackNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegister);
        NeoForge.EVENT_BUS.addListener(this::onLevelTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
        WorldPresetPack.LOGGER.info("WorldPresetPack initialized (NeoForge)");
    }

    private void onRegister(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, registry -> registry.register(
                Identifier.fromNamespaceAndPath(WorldPresetPack.MOD_ID, "void"),
                VoidChunkGenerator.CODEC
        ));
        event.register(Registries.BIOME_SOURCE, registry -> registry.register(
                Identifier.fromNamespaceAndPath(WorldPresetPack.MOD_ID, "spawn_biome"),
                SkyblockBiomeSource.CODEC
        ));
    }

    private void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldPresetPack.onLevelTick(level);
        }
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WorldPresetPack.onPlayerJoin(player);
        }
    }
}
