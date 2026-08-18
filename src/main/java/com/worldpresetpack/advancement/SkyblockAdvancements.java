package com.worldpresetpack.advancement;

import com.worldpresetpack.WorldPresetPackMod;
import com.worldpresetpack.worldgen.SkyblockWorlds;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SkyblockAdvancements {

    public static final Identifier ROOT = Identifier.fromNamespaceAndPath(WorldPresetPackMod.MOD_ID, "root");

    private SkyblockAdvancements() {}

    public static void grantRoot(ServerPlayer player, MinecraftServer server) {
        if (!SkyblockWorlds.isSkyblock(server)) {
            return;
        }
        AdvancementHolder root = server.getAdvancements().get(ROOT);
        if (root != null) {
            player.getAdvancements().award(root, "entered_world");
        }
    }
}
