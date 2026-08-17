package com.worldpresetpack.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.worldpresetpack.WorldPresetPackMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelData;

public final class SkyblockSpawnPlatform {

    /** Grass layer Y. Players stand one block above this. */
    private static final int ISLAND_SURFACE_Y = 64;

    private SkyblockSpawnPlatform() {}

    static class IslandFlag extends SavedData {
        private static final Codec<IslandFlag> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("generated", false).forGetter(f -> f.generated)
                ).apply(instance, IslandFlag::new)
        );

        static final SavedDataType<IslandFlag> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath("worldpresetpack", "island"),
                IslandFlag::new,
                CODEC,
                DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES
        );

        boolean generated;
        IslandFlag() { this.generated = false; }
        IslandFlag(boolean generated) { this.generated = generated; }
    }

    public static void register() {
        // Use START_LEVEL_TICK instead of LOAD so the spawn chunks are fully loaded
        // when we place blocks. Generation runs once per world via IslandFlag.
        ServerTickEvents.START_LEVEL_TICK.register(new ServerTickEvents.StartLevelTick() {
            @Override
            public void onStartTick(ServerLevel level) {
                if (!level.dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD)) return;
                if (!(level.getChunkSource().getGenerator() instanceof VoidChunkGenerator)) return;

                IslandFlag flag = level.getDataStorage().computeIfAbsent(IslandFlag.TYPE);
                if (flag.generated) return;

                flag.generated = true;
                flag.setDirty();

                BlockPos origin = islandOrigin(level);
                generateMainIsland(level, origin);

                // Place player on top of the grass (+1 so they stand on it)
                BlockPos spawnPos = origin.above();
                level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, spawnPos, 0f, 0f));
                teleportPlayersToIsland(level, spawnPos);

                WorldPresetPackMod.LOGGER.info("[WorldPresetPack] Island generated, spawn -> {}", spawnPos);
            }
        });

        // Vanilla spawn adjustment uses empty heightmaps and can drop the player into the void
        // even after the island exists. Snap them back if they joined on air.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ServerLevel level = player.level();
            if (!level.dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD)) return;
            if (!(level.getChunkSource().getGenerator() instanceof VoidChunkGenerator)) return;

            IslandFlag flag = level.getDataStorage().computeIfAbsent(IslandFlag.TYPE);
            if (!flag.generated) return;
            if (!level.getBlockState(player.blockPosition().below()).isAir()) return;

            BlockPos spawnPos = level.getRespawnData().pos();
            player.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        });
    }

    /**
     * Build the island at the player's (or world) spawn XZ so they do not appear in the void.
     * Surface Y is always {@link #ISLAND_SURFACE_Y} — vanilla void spawn often uses Y=0.
     */
    private static BlockPos islandOrigin(ServerLevel level) {
        BlockPos hint = level.getRespawnData().pos();
        if (!level.players().isEmpty()) {
            hint = level.players().getFirst().blockPosition();
        }
        return new BlockPos(hint.getX(), ISLAND_SURFACE_Y, hint.getZ());
    }

    private static void teleportPlayersToIsland(ServerLevel level, BlockPos spawnPos) {
        double x = spawnPos.getX() + 0.5;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5;
        for (ServerPlayer player : level.players()) {
            player.teleportTo(x, y, z);
        }
    }

    private static void generateMainIsland(ServerLevel level, BlockPos origin) {
        int[][] shape = {
            {0,0,1,1,1,1,0,0},
            {0,1,1,1,1,1,1,0},
            {1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1},
            {0,1,1,1,1,1,1,0},
            {0,0,1,1,1,0,0,0},
        };

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (shape[row][col] == 1) {
                    int dx = col - 3;
                    int dz = row - 3;
                    BlockPos pos = origin.offset(dx, 0, dz);
                    level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 3);
                    level.setBlock(pos.below(2), Blocks.DIRT.defaultBlockState(), 3);
                }
            }
        }

        // Bedrock in the island center, middle layer — last block if everything else is mined.
        level.setBlock(origin.below(), Blocks.BEDROCK.defaultBlockState(), 3);

        BlockPos trunkBase = origin.offset(1, 1, 1);
        for (int dy = 0; dy < 5; dy++) {
            level.setBlock(trunkBase.above(dy), Blocks.OAK_LOG.defaultBlockState(), 3);
        }
        placeLeafSphere(level, trunkBase.above(4));

        BlockPos chestPos = origin.offset(-1, 1, 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            fillStarterChest(chest);
        }
    }

    private static void placeLeafSphere(ServerLevel level, BlockPos center) {
        int[][] leafOffsets = {
            {-2,0,-1},{-2,0,0},{-2,0,1},
            {-1,0,-2},{-1,0,-1},{-1,0,0},{-1,0,1},{-1,0,2},
            {0,0,-2},{0,0,-1},{0,0,0},{0,0,1},{0,0,2},
            {1,0,-2},{1,0,-1},{1,0,0},{1,0,1},{1,0,2},
            {2,0,-1},{2,0,0},{2,0,1},
            {-2,1,-1},{-2,1,0},{-2,1,1},
            {-1,1,-2},{-1,1,-1},{-1,1,0},{-1,1,1},{-1,1,2},
            {0,1,-2},{0,1,-1},{0,1,1},{0,1,2},
            {1,1,-2},{1,1,-1},{1,1,0},{1,1,1},{1,1,2},
            {2,1,-1},{2,1,0},{2,1,1},
            {-1,2,-1},{-1,2,0},{-1,2,1},
            {0,2,-1},{0,2,0},{0,2,1},
            {1,2,-1},{1,2,0},{1,2,1},
            {0,3,0},
        };
        for (int[] off : leafOffsets) {
            BlockPos leafPos = center.offset(off[0], off[1], off[2]);
            if (level.getBlockState(leafPos).isAir()) {
                level.setBlock(leafPos,
                        Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
            }
        }
    }

    private static void fillStarterChest(ChestBlockEntity chest) {
        int slot = 0;
        chest.setItem(slot++, new ItemStack(Items.LAVA_BUCKET, 1));
        chest.setItem(slot++, new ItemStack(Items.WATER_BUCKET, 1));
        chest.setItem(slot++, new ItemStack(Items.ICE, 1));
        chest.setItem(slot++, new ItemStack(Items.MELON_SLICE, 1));
        chest.setItem(slot++, new ItemStack(Items.SUGAR_CANE, 2));
        chest.setItem(slot++, new ItemStack(Items.BREAD, 1));
        chest.setItem(slot++, new ItemStack(Items.BONE, 1));
        chest.setItem(slot++, new ItemStack(Items.PUMPKIN, 1));
        chest.setItem(slot, new ItemStack(Items.OBSIDIAN, 10));
    }
}
