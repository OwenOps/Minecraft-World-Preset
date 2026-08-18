package com.worldpresetpack.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.worldpresetpack.WorldPresetPackMod;
import com.worldpresetpack.config.OneBlockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class OneBlockPlatform {

    static final int BLOCK_Y = 64;
    static final BlockPos ORIGIN = new BlockPos(0, BLOCK_Y, 0);
    private static final int CHEST_EVERY = 25;
    private static final int MOB_EVERY = 50;

    private OneBlockPlatform() {}

    static final class State extends SavedData {
        private static final Codec<State> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("placed", false).forGetter(s -> s.placed),
                        Codec.INT.optionalFieldOf("broken", 0).forGetter(s -> s.broken),
                        Codec.INT.optionalFieldOf("phase", 0).forGetter(s -> s.phaseIndex),
                        Codec.STRING.optionalFieldOf("pace", OneBlockConfig.Pace.NORMAL.getSerializedName())
                                .forGetter(s -> s.pace.getSerializedName())
                ).apply(instance, State::new)
        );

        static final SavedDataType<State> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath("worldpresetpack", "oneblock"),
                State::new,
                CODEC,
                DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES
        );

        boolean placed;
        int broken;
        int phaseIndex;
        OneBlockConfig.Pace pace;

        State() {
            this(false, 0, 0, OneBlockConfig.Pace.NORMAL.getSerializedName());
        }

        State(boolean placed, int broken, int phaseIndex, String paceId) {
            this.placed = placed;
            this.broken = broken;
            this.phaseIndex = phaseIndex;
            this.pace = OneBlockConfig.Pace.fromSerialized(paceId);
        }
    }

    public static void onLevelTick(ServerLevel level) {
        if (!VoidWorlds.isOneBlock(level)) {
            return;
        }

        State state = level.getDataStorage().computeIfAbsent(State.TYPE);
        if (!state.placed) {
            state.placed = true;
            state.pace = OneBlockConfig.pace;
            state.setDirty();
            level.setBlock(ORIGIN, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            BlockPos stand = ORIGIN.above();
            VoidSpawns.setOverworldRespawn(level, stand);
            VoidSpawns.teleportPlayers(level, stand);
            WorldPresetPackMod.LOGGER.info("[WorldPresetPack] OneBlock placed at {}", ORIGIN);
            return;
        }

        if (!level.getBlockState(ORIGIN).isAir()) {
            return;
        }

        state.broken++;
        int length = OneBlockPhases.phaseLength(state.phaseIndex, state.pace);
        if (state.phaseIndex < OneBlockPhases.Phase.values().length - 1 && state.broken >= length) {
            state.broken = 0;
            state.phaseIndex++;
            announcePhase(level, state.phaseIndex);
        }
        state.setDirty();

        RandomSource random = level.getRandom();
        OneBlockPhases.Phase phase = OneBlockPhases.Phase.byIndex(state.phaseIndex);

        if (state.broken > 0 && state.broken % CHEST_EVERY == 0) {
            level.setBlock(ORIGIN, Blocks.CHEST.defaultBlockState(), 3);
            if (level.getBlockEntity(ORIGIN) instanceof ChestBlockEntity chest) {
                OneBlockPhases.fillChest(chest, phase, random);
            }
        } else {
            BlockState next = OneBlockPhases.randomBlock(phase, random).defaultBlockState();
            level.setBlock(ORIGIN, next, 3);
        }

        VoidSpawns.catchIfFallingThrough(level, ORIGIN.above());

        if (state.broken > 0 && state.broken % MOB_EVERY == 0) {
            spawnMob(level, phase, random);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        ServerLevel level = player.level();
        if (!VoidWorlds.isOneBlock(level)) {
            return;
        }
        State state = level.getDataStorage().computeIfAbsent(State.TYPE);
        if (!state.placed) {
            return;
        }
        VoidSpawns.teleportIfOnAir(player, ORIGIN.above());
    }

    private static void spawnMob(ServerLevel level, OneBlockPhases.Phase phase, RandomSource random) {
        EntityType<?> type = OneBlockPhases.randomMob(phase, random);
        type.spawn(level, ORIGIN.above(), EntitySpawnReason.EVENT);
    }

    private static void announcePhase(ServerLevel level, int phaseIndex) {
        OneBlockPhases.Phase phase = OneBlockPhases.Phase.byIndex(phaseIndex);
        Component message = Component.translatable(
                "worldpresetpack.oneblock.phase." + phase.name().toLowerCase());
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
        }
    }
}
