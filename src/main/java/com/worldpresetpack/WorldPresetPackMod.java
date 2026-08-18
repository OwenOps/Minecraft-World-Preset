package com.worldpresetpack;

import com.worldpresetpack.advancement.SkyblockAdvancements;
import com.worldpresetpack.registry.ModWorldPresets;
import com.worldpresetpack.worldgen.OneBlockPlatform;
import com.worldpresetpack.worldgen.SkyblockSpawnPlatform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric entry. Gameplay lives in worldgen / advancement classes (Minecraft APIs only).
 * A Forge port would call the same onLevelTick / onPlayerJoin / grantRoot methods.
 */
public class WorldPresetPackMod implements ModInitializer {

    public static final String MOD_ID = "worldpresetpack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModWorldPresets.register();

        ServerTickEvents.START_LEVEL_TICK.register(level -> {
            SkyblockSpawnPlatform.onLevelTick(level);
            OneBlockPlatform.onLevelTick(level);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            SkyblockSpawnPlatform.onPlayerJoin(player);
            OneBlockPlatform.onPlayerJoin(player);
            SkyblockAdvancements.grantRoot(player, server);
        });

        LOGGER.info("WorldPresetPack initialized");
    }
}
