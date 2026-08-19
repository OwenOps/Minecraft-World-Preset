package com.worldpresetpack.worldgen;

import com.worldpresetpack.config.OneBlockConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.List;

/** Weighted blocks, chest loot, and mobs per OneBlock phase. */
final class OneBlockPhases {

    private OneBlockPhases() {}

    enum Phase {
        PLAINS,
        UNDERGROUND,
        WINTER,
        OCEAN,
        JUNGLE,
        NETHER,
        END;

        static Phase byIndex(int index) {
            Phase[] values = values();
            if (index < 0) {
                return PLAINS;
            }
            if (index >= values.length) {
                return END;
            }
            return values[index];
        }
    }

    private record WeightedBlock(Block block, int weight) {}

    private static final int[] BASE_LENGTHS = {80, 128, 80, 80, 80, 128, 80};

    static int phaseLength(int phaseIndex, OneBlockConfig.Pace pace) {
        int index = Math.min(phaseIndex, BASE_LENGTHS.length - 1);
        return Math.max(24, Math.round(BASE_LENGTHS[index] * pace.phaseLengthMultiplier()));
    }

    static Block randomBlock(Phase phase, RandomSource random) {
        List<WeightedBlock> pool = switch (phase) {
            case PLAINS -> List.of(
                    new WeightedBlock(Blocks.GRASS_BLOCK, 18),
                    new WeightedBlock(Blocks.DIRT, 14),
                    new WeightedBlock(Blocks.OAK_LOG, 10),
                    new WeightedBlock(Blocks.OAK_LEAVES, 8),
                    new WeightedBlock(Blocks.COBBLESTONE, 12),
                    new WeightedBlock(Blocks.COAL_ORE, 6),
                    new WeightedBlock(Blocks.GRAVEL, 8),
                    new WeightedBlock(Blocks.SAND, 8),
                    new WeightedBlock(Blocks.CLAY, 4),
                    new WeightedBlock(Blocks.OAK_PLANKS, 4),
                    new WeightedBlock(Blocks.PUMPKIN, 2),
                    new WeightedBlock(Blocks.MELON, 2)
            );
            case UNDERGROUND -> List.of(
                    new WeightedBlock(Blocks.STONE, 22),
                    new WeightedBlock(Blocks.COBBLESTONE, 12),
                    new WeightedBlock(Blocks.GRANITE, 6),
                    new WeightedBlock(Blocks.DIORITE, 6),
                    new WeightedBlock(Blocks.ANDESITE, 6),
                    new WeightedBlock(Blocks.COAL_ORE, 10),
                    new WeightedBlock(Blocks.IRON_ORE, 8),
                    new WeightedBlock(Blocks.COPPER_ORE, 6),
                    new WeightedBlock(Blocks.GRAVEL, 8),
                    new WeightedBlock(Blocks.DEEPSLATE, 8),
                    new WeightedBlock(Blocks.TUFF, 4),
                    new WeightedBlock(Blocks.GOLD_ORE, 2)
            );
            case WINTER -> List.of(
                    new WeightedBlock(Blocks.SNOW_BLOCK, 18),
                    new WeightedBlock(Blocks.ICE, 14),
                    new WeightedBlock(Blocks.PACKED_ICE, 6),
                    new WeightedBlock(Blocks.SPRUCE_LOG, 12),
                    new WeightedBlock(Blocks.SPRUCE_LEAVES, 8),
                    new WeightedBlock(Blocks.DIRT, 10),
                    new WeightedBlock(Blocks.COBBLESTONE, 10),
                    new WeightedBlock(Blocks.COAL_ORE, 6),
                    new WeightedBlock(Blocks.BLUE_ICE, 2)
            );
            case OCEAN -> List.of(
                    new WeightedBlock(Blocks.SAND, 18),
                    new WeightedBlock(Blocks.GRAVEL, 10),
                    new WeightedBlock(Blocks.SANDSTONE, 8),
                    new WeightedBlock(Blocks.PRISMARINE, 8),
                    new WeightedBlock(Blocks.DARK_PRISMARINE, 4),
                    new WeightedBlock(Blocks.SEA_LANTERN, 3),
                    new WeightedBlock(Blocks.CLAY, 8),
                    new WeightedBlock(Blocks.DRIED_KELP_BLOCK, 6),
                    new WeightedBlock(Blocks.TUBE_CORAL_BLOCK, 4),
                    new WeightedBlock(Blocks.SPONGE, 2)
            );
            case JUNGLE -> List.of(
                    new WeightedBlock(Blocks.JUNGLE_LOG, 14),
                    new WeightedBlock(Blocks.JUNGLE_LEAVES, 10),
                    new WeightedBlock(Blocks.MOSS_BLOCK, 8),
                    new WeightedBlock(Blocks.MELON, 6),
                    new WeightedBlock(Blocks.DIRT, 10),
                    new WeightedBlock(Blocks.COBBLESTONE, 8),
                    new WeightedBlock(Blocks.BAMBOO_BLOCK, 6),
                    new WeightedBlock(Blocks.GOLD_ORE, 3),
                    new WeightedBlock(Blocks.MOSSY_COBBLESTONE, 6)
            );
            case NETHER -> List.of(
                    new WeightedBlock(Blocks.NETHERRACK, 22),
                    new WeightedBlock(Blocks.SOUL_SAND, 8),
                    new WeightedBlock(Blocks.SOUL_SOIL, 5),
                    new WeightedBlock(Blocks.GLOWSTONE, 6),
                    new WeightedBlock(Blocks.MAGMA_BLOCK, 6),
                    new WeightedBlock(Blocks.NETHER_QUARTZ_ORE, 8),
                    new WeightedBlock(Blocks.BLACKSTONE, 10),
                    new WeightedBlock(Blocks.BASALT, 8),
                    new WeightedBlock(Blocks.NETHER_BRICKS, 5),
                    new WeightedBlock(Blocks.ANCIENT_DEBRIS, 1)
            );
            case END -> List.of(
                    new WeightedBlock(Blocks.END_STONE, 22),
                    new WeightedBlock(Blocks.PURPUR_BLOCK, 8),
                    new WeightedBlock(Blocks.OBSIDIAN, 8),
                    new WeightedBlock(Blocks.END_STONE_BRICKS, 6),
                    new WeightedBlock(Blocks.PURPUR_PILLAR, 4),
                    new WeightedBlock(Blocks.END_ROD, 3),
                    new WeightedBlock(Blocks.DIAMOND_ORE, 2),
                    new WeightedBlock(Blocks.ANCIENT_DEBRIS, 1)
            );
        };
        return pick(pool, random);
    }

    static void fillChest(ChestBlockEntity chest, Phase phase, RandomSource random) {
        List<ItemStack> loot = switch (phase) {
            case PLAINS -> List.of(
                    new ItemStack(Items.OAK_SAPLING, 2),
                    new ItemStack(Items.WHEAT_SEEDS, 4),
                    new ItemStack(Items.BONE, 2),
                    new ItemStack(Items.ICE, 2),
                    new ItemStack(Items.BREAD, 2),
                    new ItemStack(Items.WATER_BUCKET)
            );
            case UNDERGROUND -> List.of(
                    new ItemStack(Items.COAL, 8),
                    new ItemStack(Items.IRON_INGOT, 3),
                    new ItemStack(Items.LAVA_BUCKET),
                    new ItemStack(Items.TORCH, 8),
                    new ItemStack(Items.APPLE, 2)
            );
            case WINTER -> List.of(
                    new ItemStack(Items.SPRUCE_SAPLING, 2),
                    new ItemStack(Items.SNOWBALL, 8),
                    new ItemStack(Items.SWEET_BERRIES, 4),
                    new ItemStack(Items.PACKED_ICE, 2)
            );
            case OCEAN -> List.of(
                    new ItemStack(Items.KELP, 8),
                    new ItemStack(Items.PRISMARINE_SHARD, 4),
                    new ItemStack(Items.COD, 3),
                    new ItemStack(Items.HEART_OF_THE_SEA)
            );
            case JUNGLE -> List.of(
                    new ItemStack(Items.JUNGLE_SAPLING, 2),
                    new ItemStack(Items.BAMBOO, 6),
                    new ItemStack(Items.COCOA_BEANS, 4),
                    new ItemStack(Items.MELON_SLICE, 4)
            );
            case NETHER -> List.of(
                    new ItemStack(Items.NETHER_WART, 4),
                    new ItemStack(Items.GOLD_INGOT, 4),
                    new ItemStack(Items.BLAZE_ROD, 1),
                    new ItemStack(Items.OBSIDIAN, 4)
            );
            case END -> List.of(
                    new ItemStack(Items.ENDER_PEARL, 4),
                    new ItemStack(Items.CHORUS_FRUIT, 4),
                    new ItemStack(Items.DIAMOND, 1),
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 4)
            );
        };
        int slots = 2 + random.nextInt(3);
        for (int i = 0; i < slots && i < loot.size(); i++) {
            ItemStack stack = loot.get(random.nextInt(loot.size())).copy();
            chest.setItem(i, stack);
        }
    }

    static EntityType<?> randomMob(Phase phase, RandomSource random) {
        List<EntityType<?>> mobs = switch (phase) {
            case PLAINS -> List.of(EntityTypes.CHICKEN, EntityTypes.PIG, EntityTypes.COW, EntityTypes.SHEEP, EntityTypes.ZOMBIE);
            case UNDERGROUND -> List.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.CREEPER, EntityTypes.SPIDER, EntityTypes.BAT);
            case WINTER -> List.of(EntityTypes.STRAY, EntityTypes.POLAR_BEAR, EntityTypes.RABBIT, EntityTypes.WOLF);
            case OCEAN -> List.of(EntityTypes.DROWNED, EntityTypes.SPIDER, EntityTypes.ZOMBIE, EntityTypes.GUARDIAN);
            case JUNGLE -> List.of(EntityTypes.PARROT, EntityTypes.OCELOT, EntityTypes.SPIDER, EntityTypes.PANDA);
            case NETHER -> List.of(EntityTypes.ZOMBIFIED_PIGLIN, EntityTypes.MAGMA_CUBE, EntityTypes.BLAZE, EntityTypes.PIGLIN);
            case END -> List.of(EntityTypes.ENDERMAN, EntityTypes.ENDERMITE, EntityTypes.PHANTOM);
        };
        return mobs.get(random.nextInt(mobs.size()));
    }

    private static Block pick(List<WeightedBlock> pool, RandomSource random) {
        int total = 0;
        for (WeightedBlock entry : pool) {
            total += entry.weight;
        }
        int roll = random.nextInt(total);
        int cursor = 0;
        for (WeightedBlock entry : pool) {
            cursor += entry.weight;
            if (roll < cursor) {
                return entry.block;
            }
        }
        return pool.getLast().block;
    }
}
