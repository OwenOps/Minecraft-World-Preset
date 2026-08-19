package com.worldpresetpack.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldPresetPackClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("worldpresetpack-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("WorldPresetPack client initialized");
    }
}
