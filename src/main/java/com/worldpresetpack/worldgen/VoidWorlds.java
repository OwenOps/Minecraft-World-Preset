package com.worldpresetpack.worldgen;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * Which void preset a save is. Uses only Minecraft APIs so Forge can call the same checks.
 */
public final class VoidWorlds {

    private VoidWorlds() {}

    public static VoidChunkGenerator overworldGenerator(MinecraftServer server) {
        if (server == null) {
            return null;
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return null;
        }
        if (overworld.getChunkSource().getGenerator() instanceof VoidChunkGenerator generator) {
            return generator;
        }
        return null;
    }

    public static VoidChunkGenerator overworldGenerator(ServerLevel level) {
        if (!level.dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD)) {
            return null;
        }
        if (level.getChunkSource().getGenerator() instanceof VoidChunkGenerator generator) {
            return generator;
        }
        return null;
    }

    public static boolean isSkyblock(MinecraftServer server) {
        VoidChunkGenerator generator = overworldGenerator(server);
        return generator != null && generator.isSkyblock();
    }

    public static boolean isSkyblock(ServerLevel level) {
        VoidChunkGenerator generator = overworldGenerator(level);
        return generator != null && generator.isSkyblock();
    }

    public static boolean isOneBlock(ServerLevel level) {
        VoidChunkGenerator generator = overworldGenerator(level);
        return generator != null && generator.isOneBlock();
    }
}
