package com.worldpresetpack.worldgen;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * A save is Skyblock when the Overworld void generator has kind skyblock.
 * Nether / End of that same save inherit the classification.
 */
public final class SkyblockWorlds {

    private SkyblockWorlds() {}

    public static boolean isSkyblock(MinecraftServer server) {
        return VoidWorlds.isSkyblock(server);
    }

    public static boolean isSkyblock(ServerLevel level) {
        return VoidWorlds.isSkyblock(level);
    }
}
