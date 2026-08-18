package com.worldpresetpack.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.worldpresetpack.WorldPresetPackMod;
import com.worldpresetpack.config.SkyblockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

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

    public static void onLevelTick(ServerLevel level) {
        if (!VoidWorlds.isSkyblock(level)) {
            return;
        }

        IslandFlag flag = level.getDataStorage().computeIfAbsent(IslandFlag.TYPE);
        if (flag.generated) {
            return;
        }

        flag.generated = true;
        flag.setDirty();

        BlockPos origin = islandOrigin(level);
        generateMainIsland(level, origin, difficultyOf(level));

        BlockPos spawnPos = origin.above();
        VoidSpawns.setOverworldRespawn(level, spawnPos);
        VoidSpawns.teleportPlayers(level, spawnPos);

        WorldPresetPackMod.LOGGER.info("[WorldPresetPack] Island generated, spawn -> {}", spawnPos);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        ServerLevel level = player.level();
        if (!VoidWorlds.isSkyblock(level)) {
            return;
        }

        IslandFlag flag = level.getDataStorage().computeIfAbsent(IslandFlag.TYPE);
        if (!flag.generated) {
            return;
        }
        VoidSpawns.teleportIfOnAir(player, level.getRespawnData().pos());
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

    private static SkyblockConfig.Difficulty difficultyOf(ServerLevel level) {
        VoidChunkGenerator generator = VoidWorlds.overworldGenerator(level);
        return generator != null ? generator.difficulty() : SkyblockConfig.Difficulty.CLASSIC;
    }

    private static void generateMainIsland(ServerLevel level, BlockPos origin, SkyblockConfig.Difficulty difficulty) {
        int[][] shape = islandShape(difficulty);
        int originCol = islandOriginIndex(difficulty);

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    int dx = col - originCol;
                    int dz = row - originCol;
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
        placeLeafSphere(level, trunkBase);

        BlockPos chestPos = origin.offset(-1, 1, 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            fillStarterChest(chest, difficulty);
        }
        placeSnowIfCold(level, origin, shape, originCol);
    }

    private static void placeSnowIfCold(ServerLevel level, BlockPos origin, int[][] shape, int originCol) {
        Holder<Biome> biome = level.getBiome(origin);
        if (!biome.is(BiomeTags.SPAWNS_SNOW_FOXES)
                && !biome.is(Biomes.FROZEN_OCEAN)
                && !biome.is(Biomes.ICE_SPIKES)) {
            return;
        }
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 1) {
                    continue;
                }
                BlockPos above = origin.offset(col - originCol, 1, row - originCol);
                if (level.getBlockState(above).isAir()) {
                    level.setBlock(above, Blocks.SNOW.defaultBlockState(), 3);
                }
            }
        }
    }

    /** Classic keeps its original 8×8 mask and origin at index 3. */
    private static int islandOriginIndex(SkyblockConfig.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 4;
            case CLASSIC -> 3;
            case HARD -> 2;
        };
    }

    private static int[][] islandShape(SkyblockConfig.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new int[][] {
                    {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                    {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
                    {0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                    {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
                    {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
            };
            case CLASSIC -> new int[][] {
                    {0, 0, 1, 1, 1, 1, 0, 0},
                    {0, 1, 1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 1, 1, 1, 0},
                    {0, 0, 1, 1, 1, 0, 0, 0},
            };
            case HARD -> new int[][] {
                    {0, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 0},
            };
        };
    }

    /**
     * Oak canopy. Leaves are not persistent: distance to the trunk is set so they stay
     * until the logs are cut, then decay like a vanilla tree (saplings / apples).
     */
    private static void placeLeafSphere(ServerLevel level, BlockPos trunkBase) {
        BlockPos center = trunkBase.above(4);
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
                int distance = Math.max(1, Math.min(6, manhattanToTrunk(leafPos, trunkBase)));
                level.setBlock(leafPos,
                        Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, distance), 3);
            }
        }
    }

    private static int manhattanToTrunk(BlockPos leaf, BlockPos trunkBase) {
        int best = LeavesBlock.DECAY_DISTANCE;
        for (int dy = 0; dy < 5; dy++) {
            BlockPos log = trunkBase.above(dy);
            int d = Math.abs(leaf.getX() - log.getX())
                    + Math.abs(leaf.getY() - log.getY())
                    + Math.abs(leaf.getZ() - log.getZ());
            if (d < best) {
                best = d;
            }
        }
        return best;
    }

    private static void fillStarterChest(ChestBlockEntity chest, SkyblockConfig.Difficulty difficulty) {
        int slot = 0;
        chest.setItem(slot++, new ItemStack(Items.LAVA_BUCKET, 1));
        chest.setItem(slot++, new ItemStack(Items.WATER_BUCKET, 1));
        chest.setItem(slot++, new ItemStack(Items.ICE, difficulty == SkyblockConfig.Difficulty.EASY ? 2 : 1));

        switch (difficulty) {
            case EASY -> {
                chest.setItem(slot++, new ItemStack(Items.MELON_SLICE, 2));
                chest.setItem(slot++, new ItemStack(Items.SUGAR_CANE, 4));
                chest.setItem(slot++, new ItemStack(Items.BREAD, 2));
                chest.setItem(slot++, new ItemStack(Items.BONE, 2));
                chest.setItem(slot++, new ItemStack(Items.PUMPKIN, 1));
                chest.setItem(slot++, new ItemStack(Items.BROWN_MUSHROOM, 1));
            }
            case CLASSIC -> {
                chest.setItem(slot++, new ItemStack(Items.MELON_SLICE, 1));
                chest.setItem(slot++, new ItemStack(Items.SUGAR_CANE, 2));
                chest.setItem(slot++, new ItemStack(Items.BREAD, 1));
                chest.setItem(slot++, new ItemStack(Items.BONE, 1));
                chest.setItem(slot++, new ItemStack(Items.PUMPKIN, 1));
            }
            case HARD -> chest.setItem(slot++, new ItemStack(Items.SUGAR_CANE, 1));
        }

        chest.setItem(slot, new ItemStack(Items.OBSIDIAN, 10));
    }
}
