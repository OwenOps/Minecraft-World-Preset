package com.worldpresetpack.mixin.accessor;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(StructurePlacement.class)
public interface StructurePlacementAccessor {

    @Invoker("locateOffset")
    Vec3i worldpresetpack$locateOffset();

    @Invoker("frequencyReductionMethod")
    StructurePlacement.FrequencyReductionMethod worldpresetpack$frequencyReductionMethod();

    @Invoker("frequency")
    float worldpresetpack$frequency();

    @Invoker("salt")
    int worldpresetpack$salt();

    @Invoker("exclusionZone")
    Optional<StructurePlacement.ExclusionZone> worldpresetpack$exclusionZone();
}
