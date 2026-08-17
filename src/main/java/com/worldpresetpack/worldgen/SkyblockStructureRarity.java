package com.worldpresetpack.worldgen;

import com.worldpresetpack.mixin.accessor.ChunkGeneratorStructureStateAccessor;
import com.worldpresetpack.mixin.accessor.StructurePlacementAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Copies vanilla structure sets with wider spacing / lower frequency
 * for the common ones (villages, shipwrecks, mineshafts, …).
 * Already-rare sets keep vanilla placement. Skyblock only.
 */
public final class SkyblockStructureRarity {

    /** Spacing multiplier for common structures (~villages 34 → 85 chunks). */
    private static final double SPACING_MULTIPLIER = 2.5;

    /** Frequency multiplier for chance-based common sets (mineshafts, buried treasure, …). */
    private static final float FREQUENCY_MULTIPLIER = 0.5f;

    /** Already sparse in vanilla — do not make them rarer. */
    private static final Set<String> VANILLA_RARITY = Set.of(
            "strongholds",
            "pillager_outposts",
            "ocean_monuments",
            "woodland_mansions",
            "ancient_cities",
            "desert_pyramids",
            "jungle_temples",
            "igloos",
            "swamp_huts",
            "trail_ruins",
            "end_cities"
    );

    /** Ruined portals skip a lot of Skyblock progression if they spawn nearby. */
    private static final Set<String> VERY_RARE_SPACING = Set.of("ruined_portals");

    private static final double VERY_RARE_SPACING_MULTIPLIER = 8.0;

    private static final Set<String> VERY_RARE_FREQUENCY = Set.of("buried_treasures", "ruined_portals");

    private SkyblockStructureRarity() {}

    public static ChunkGeneratorStructureState createState(
            HolderLookup<StructureSet> structureSets,
            RandomState randomState,
            long seed,
            BiomeSource biomeSource) {
        List<Holder.Reference<StructureSet>> originals = structureSets.listElements().toList();

        Map<ResourceKey<StructureSet>, Holder<StructureSet>> firstPass = new HashMap<>();
        for (Holder.Reference<StructureSet> ref : originals) {
            StructureSet rarefied = rarefy(ref.key(), ref.value(), Map.of());
            firstPass.put(ref.key(), Holder.direct(rarefied));
        }

        List<Holder<StructureSet>> rebuilt = new ArrayList<>(originals.size());
        for (Holder.Reference<StructureSet> ref : originals) {
            rebuilt.add(Holder.direct(rarefy(ref.key(), ref.value(), firstPass)));
        }

        // Filter to structure sets that can actually spawn in this dimension's biomes,
        // then rebuild with the world seed so stronghold rings are not identical every save.
        ChunkGeneratorStructureState filtered = ChunkGeneratorStructureState.createForFlat(
                randomState, seed, biomeSource, rebuilt.stream());
        return ChunkGeneratorStructureStateAccessor.worldpresetpack$create(
                randomState, biomeSource, seed, seed, filtered.possibleStructureSets());
    }

    private static StructureSet rarefy(
            ResourceKey<StructureSet> key,
            StructureSet original,
            Map<ResourceKey<StructureSet>, Holder<StructureSet>> remappedOthers) {
        return new StructureSet(original.structures(), scalePlacement(key, original.placement(), remappedOthers));
    }

    private static StructurePlacement scalePlacement(
            ResourceKey<StructureSet> key,
            StructurePlacement placement,
            Map<ResourceKey<StructureSet>, Holder<StructureSet>> remappedOthers) {
        StructurePlacementAccessor acc = (StructurePlacementAccessor) placement;
        Optional<StructurePlacement.ExclusionZone> exclusion =
                remapExclusion(acc.worldpresetpack$exclusionZone(), remappedOthers);

        if (keepsVanillaRarity(key)) {
            return withExclusion(placement, exclusion);
        }

        if (placement instanceof RandomSpreadStructurePlacement random) {
            double spacingMul = spacingMultiplier(key);
            int spacing = scaleInt(random.spacing(), spacingMul);
            int separation = scaleInt(random.separation(), spacingMul);
            if (separation >= spacing) {
                separation = Math.max(0, spacing - 1);
            }
            return new RandomSpreadStructurePlacement(
                    acc.worldpresetpack$locateOffset(),
                    acc.worldpresetpack$frequencyReductionMethod(),
                    scaleFrequency(key, acc.worldpresetpack$frequency()),
                    acc.worldpresetpack$salt(),
                    exclusion,
                    spacing,
                    separation,
                    random.spreadType());
        }

        return withExclusion(placement, exclusion);
    }

    private static boolean keepsVanillaRarity(ResourceKey<StructureSet> key) {
        return VANILLA_RARITY.contains(key.identifier().getPath());
    }

    /** Same placement numbers, possibly remapped exclusion zone (e.g. outposts vs rarer villages). */
    private static StructurePlacement withExclusion(
            StructurePlacement placement,
            Optional<StructurePlacement.ExclusionZone> exclusion) {
        StructurePlacementAccessor acc = (StructurePlacementAccessor) placement;
        if (exclusion.equals(acc.worldpresetpack$exclusionZone())) {
            return placement;
        }

        if (placement instanceof RandomSpreadStructurePlacement random) {
            return new RandomSpreadStructurePlacement(
                    acc.worldpresetpack$locateOffset(),
                    acc.worldpresetpack$frequencyReductionMethod(),
                    acc.worldpresetpack$frequency(),
                    acc.worldpresetpack$salt(),
                    exclusion,
                    random.spacing(),
                    random.separation(),
                    random.spreadType());
        }

        if (placement instanceof ConcentricRingsStructurePlacement rings) {
            return new ConcentricRingsStructurePlacement(
                    acc.worldpresetpack$locateOffset(),
                    acc.worldpresetpack$frequencyReductionMethod(),
                    acc.worldpresetpack$frequency(),
                    acc.worldpresetpack$salt(),
                    exclusion,
                    rings.distance(),
                    rings.spread(),
                    rings.count(),
                    rings.preferredBiomes());
        }

        return placement;
    }

    private static Optional<StructurePlacement.ExclusionZone> remapExclusion(
            Optional<StructurePlacement.ExclusionZone> zone,
            Map<ResourceKey<StructureSet>, Holder<StructureSet>> remappedOthers) {
        if (zone.isEmpty()) return Optional.empty();
        if (remappedOthers.isEmpty()) return zone;

        StructurePlacement.ExclusionZone original = zone.get();
        return original.otherSet().unwrapKey()
                .map(key -> remappedOthers.getOrDefault(key, original.otherSet()))
                .map(holder -> new StructurePlacement.ExclusionZone(holder, original.chunkCount()))
                .or(() -> zone);
    }

    private static double spacingMultiplier(ResourceKey<StructureSet> key) {
        return VERY_RARE_SPACING.contains(key.identifier().getPath())
                ? VERY_RARE_SPACING_MULTIPLIER
                : SPACING_MULTIPLIER;
    }

    private static float scaleFrequency(ResourceKey<StructureSet> key, float frequency) {
        if (frequency >= 1.0f) return frequency;
        float scaled = frequency * FREQUENCY_MULTIPLIER;
        if (VERY_RARE_FREQUENCY.contains(key.identifier().getPath())) {
            scaled *= FREQUENCY_MULTIPLIER;
        }
        return Math.max(0.0001f, scaled);
    }

    private static int scaleInt(int value, double multiplier) {
        if (value <= 0) return 0;
        return Math.max(1, (int) Math.round(value * multiplier));
    }
}
