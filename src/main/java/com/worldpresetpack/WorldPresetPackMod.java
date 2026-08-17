package com.worldpresetpack;

import com.worldpresetpack.advancement.SkyblockAdvancements;
import com.worldpresetpack.registry.ModWorldPresets;
import com.worldpresetpack.worldgen.SkyblockSpawnPlatform;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldPresetPackMod implements ModInitializer {

    public static final String MOD_ID = "worldpresetpack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModWorldPresets.register();
        SkyblockSpawnPlatform.register();
        SkyblockAdvancements.register();
        LOGGER.info("WorldPresetPack initialized");
    }
}
