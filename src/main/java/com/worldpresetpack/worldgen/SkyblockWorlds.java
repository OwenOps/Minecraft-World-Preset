package com.worldpresetpack.worldgen;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * A save is Skyblock when the Overworld uses {@link VoidChunkGenerator}.
 * Nether / End of that same save inherit the classification.
 */
public final class SkyblockWorlds {

    private SkyblockWorlds() {}

    public static boolean isSkyblock(MinecraftServer server) {
        if (server == null) return false;
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld != null
                && overworld.getChunkSource().getGenerator() instanceof VoidChunkGenerator;
    }
}
