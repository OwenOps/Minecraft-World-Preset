package com.worldpresetpack;

import com.worldpresetpack.advancement.SkyblockAdvancements;
import com.worldpresetpack.worldgen.OneBlockPlatform;
import com.worldpresetpack.worldgen.SkyblockSpawnPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic gameplay hooks. Fabric and NeoForge call these from their own events.
 */
public final class WorldPresetPack {

    public static final String MOD_ID = "worldpresetpack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private WorldPresetPack() {}

    public static void onLevelTick(ServerLevel level) {
        SkyblockSpawnPlatform.onLevelTick(level);
        OneBlockPlatform.onLevelTick(level);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        SkyblockSpawnPlatform.onPlayerJoin(player);
        OneBlockPlatform.onPlayerJoin(player);
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            SkyblockAdvancements.grantRoot(player, server);
        }
    }
}
