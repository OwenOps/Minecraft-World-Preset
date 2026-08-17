package com.worldpresetpack.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

/**
 * Vanilla multi-noise biomes, with a square around world origin forced to {@code spawnBiome}
 * so the Skyblock island starts in the climate the player picked.
 */
public final class SkyblockBiomeSource extends BiomeSource {

    /** Chebyshev radius in blocks (16 chunks). Island stays near 0,0 with structure exclusion. */
    public static final int DEFAULT_RADIUS_BLOCKS = 256;

    public static final MapCodec<SkyblockBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("background").forGetter(s -> s.background),
                    Biome.CODEC.fieldOf("spawn_biome").forGetter(s -> s.spawnBiome),
                    Codec.INT.optionalFieldOf("radius", DEFAULT_RADIUS_BLOCKS).forGetter(s -> s.radiusBlocks)
            ).apply(instance, SkyblockBiomeSource::new)
    );

    private final BiomeSource background;
    private final Holder<Biome> spawnBiome;
    private final int radiusBlocks;

    public SkyblockBiomeSource(BiomeSource background, Holder<Biome> spawnBiome, int radiusBlocks) {
        this.background = background;
        this.spawnBiome = spawnBiome;
        this.radiusBlocks = radiusBlocks;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(background.possibleBiomes().stream(), Stream.of(spawnBiome)).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(quartX);
        int blockZ = QuartPos.toBlock(quartZ);
        if (Math.abs(blockX) <= radiusBlocks && Math.abs(blockZ) <= radiusBlocks) {
            return spawnBiome;
        }
        return background.getNoiseBiome(quartX, quartY, quartZ, sampler);
    }
}
