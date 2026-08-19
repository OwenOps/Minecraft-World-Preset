package com.worldpresetpack;

import com.worldpresetpack.registry.ModWorldPresets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Fabric entry. Gameplay lives in {@link WorldPresetPack} (Minecraft APIs only).
 */
public class WorldPresetPackMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ModWorldPresets.register();

        ServerTickEvents.START_LEVEL_TICK.register(WorldPresetPack::onLevelTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                WorldPresetPack.onPlayerJoin(handler.getPlayer()));

        WorldPresetPack.LOGGER.info("WorldPresetPack initialized (Fabric)");
    }
}
