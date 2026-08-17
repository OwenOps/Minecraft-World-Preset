package com.worldpresetpack.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.worldpresetpack.WorldPresetPackMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
        // when we place blocks. The listener unregisters itself after first generation.
        ServerTickEvents.START_LEVEL_TICK.register(new ServerTickEvents.StartLevelTick() {
            @Override
            public void onStartTick(ServerLevel level) {
                if (!level.dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD)) return;
                if (!(level.getChunkSource().getGenerator() instanceof VoidChunkGenerator)) return;

                IslandFlag flag = level.getDataStorage().computeIfAbsent(IslandFlag.TYPE);
                if (flag.generated) return;

                flag.generated = true;
                flag.setDirty();

                BlockPos origin = new BlockPos(8, 64, 8);
                generateMainIsland(level, origin);

                // Place player on top of the grass (+1 so they stand on it)
                BlockPos spawnPos = origin.above();
                level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, spawnPos, 0f, 0f));

                WorldPresetPackMod.LOGGER.info("[WorldPresetPack] Island generated, spawn -> {}", spawnPos);
            }
        });
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
                }
            }
        }

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
