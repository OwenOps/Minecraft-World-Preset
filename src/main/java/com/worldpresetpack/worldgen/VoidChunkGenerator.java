package com.worldpresetpack.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.worldpresetpack.config.SkyblockConfig;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class VoidChunkGenerator extends ChunkGenerator {

    public enum Kind implements StringRepresentable {
        SKYBLOCK("skyblock"),
        ONE_BLOCK("one_block");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    public static final MapCodec<VoidChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(g -> g.noiseSettings),
                    Codec.BOOL.fieldOf("generate_structures").orElse(false).forGetter(g -> g.generateStructures),
                    SkyblockConfig.Difficulty.CODEC.optionalFieldOf("difficulty", SkyblockConfig.Difficulty.CLASSIC)
                            .forGetter(g -> g.difficulty),
                    Kind.CODEC.optionalFieldOf("kind", Kind.SKYBLOCK).forGetter(g -> g.kind)
            ).apply(instance, VoidChunkGenerator::new)
    );

    /** Noise settings exposed so ChunkMap can build a proper RandomState + Climate.Sampler. */
    private final Holder<NoiseGeneratorSettings> noiseSettings;
    private final boolean generateStructures;
    private final SkyblockConfig.Difficulty difficulty;
    private final Kind kind;

    public VoidChunkGenerator(BiomeSource biomeSource,
                               Holder<NoiseGeneratorSettings> noiseSettings,
                               boolean generateStructures,
                               SkyblockConfig.Difficulty difficulty) {
        this(biomeSource, noiseSettings, generateStructures, difficulty, Kind.SKYBLOCK);
    }

    public VoidChunkGenerator(BiomeSource biomeSource,
                               Holder<NoiseGeneratorSettings> noiseSettings,
                               boolean generateStructures,
                               SkyblockConfig.Difficulty difficulty,
                               Kind kind) {
        super(biomeSource);
        this.noiseSettings = noiseSettings;
        this.generateStructures = generateStructures;
        this.difficulty = difficulty;
        this.kind = kind;
    }

    /** Mirrors NoiseBasedChunkGenerator.generatorSettings() so ChunkMap can use real noise. */
    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return noiseSettings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk) {}

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager,
                             RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random,
                                                        StructureManager structureManager,
                                                        ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getGenDepth() { return 384; }

    @Override
    public int getSeaLevel() { return -63; }

    @Override
    public int getMinY() { return -64; }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                             LevelHeightAccessor level, RandomState random) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {}

    // Structures start spawning at least this many chunks away from origin (spawn island).
    // 20 chunks = 320 blocks — enough to not see any structure from spawn.
    private static final int SPAWN_EXCLUSION_CHUNKS = 20;

    @Override
    public ChunkGeneratorStructureState createState(
            HolderLookup<StructureSet> structureSets, RandomState randomState, long seed) {
        if (!generateStructures) {
            return ChunkGeneratorStructureState.createForFlat(
                    randomState, seed, getBiomeSource(), Stream.empty());
        }
        if (kind == Kind.SKYBLOCK) {
            return SkyblockStructureRarity.createState(structureSets, randomState, seed, getBiomeSource());
        }
        return super.createState(structureSets, randomState, seed);
    }

    @Override
    public void createStructures(net.minecraft.core.RegistryAccess registryAccess,
                                 ChunkGeneratorStructureState structureState,
                                 StructureManager structureManager,
                                 ChunkAccess chunk,
                                 StructureTemplateManager templateManager,
                                 ResourceKey<Level> dimension) {
        if (!generateStructures) return;

        // Block structures within SPAWN_EXCLUSION_CHUNKS of the island origin (0,0)
        net.minecraft.world.level.ChunkPos cp = chunk.getPos();
        if (Math.abs(cp.x()) < SPAWN_EXCLUSION_CHUNKS && Math.abs(cp.z()) < SPAWN_EXCLUSION_CHUNKS) return;

        super.createStructures(registryAccess, structureState, structureManager, chunk, templateManager, dimension);
    }

    public boolean isGenerateStructures() {
        return generateStructures;
    }

    public SkyblockConfig.Difficulty difficulty() {
        return difficulty;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isSkyblock() {
        return kind == Kind.SKYBLOCK;
    }

    public boolean isOneBlock() {
        return kind == Kind.ONE_BLOCK;
    }
}

