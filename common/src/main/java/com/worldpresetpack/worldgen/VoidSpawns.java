package com.worldpresetpack.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;

/** Shared spawn snaps for void presets (Skyblock island, OneBlock). */
public final class VoidSpawns {

    private VoidSpawns() {}

    public static void setOverworldRespawn(ServerLevel level, BlockPos stand) {
        level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, stand, 0f, 0f));
    }

    public static void teleportPlayers(ServerLevel level, BlockPos stand) {
        double x = stand.getX() + 0.5;
        double y = stand.getY();
        double z = stand.getZ() + 0.5;
        for (ServerPlayer player : level.players()) {
            player.teleportTo(x, y, z);
        }
    }

    public static void teleportIfOnAir(ServerPlayer player, BlockPos stand) {
        if (!player.level().getBlockState(player.blockPosition().below()).isAir()) {
            return;
        }
        player.teleportTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
    }

    /**
     * If the player just broke the block under their feet and started to fall,
     * put them back on top. Does not save someone already deep in the void.
     */
    public static void catchIfFallingThrough(ServerLevel level, BlockPos stand) {
        double standX = stand.getX() + 0.5;
        double standZ = stand.getZ() + 0.5;
        for (ServerPlayer player : level.players()) {
            if (Math.abs(player.getX() - standX) > 0.9 || Math.abs(player.getZ() - standZ) > 0.9) {
                continue;
            }
            if (player.getY() >= stand.getY() - 0.01) {
                continue;
            }
            if (player.getY() < stand.getY() - 2.5) {
                continue;
            }
            player.teleportTo(standX, stand.getY(), standZ);
        }
    }
}
