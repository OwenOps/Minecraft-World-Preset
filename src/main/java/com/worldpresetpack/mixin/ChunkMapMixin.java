package com.worldpresetpack.mixin;

import com.worldpresetpack.worldgen.VoidChunkGenerator;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the ChunkMap constructor so VoidChunkGenerator supplies real
 * NoiseGeneratorSettings instead of dummy(), giving RandomState a proper
 * Climate.Sampler so MultiNoiseBiomeSource returns varied biomes.
 *
 * Uses a static ThreadLocal to pass the generator from the @Inject (before super)
 * to the @Redirect, avoiding the Mixin restriction on writing instance fields
 * before super() completes.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    // ThreadLocal so the generator captured before super() is accessible in the redirect.
    private static final ThreadLocal<ChunkGenerator> GENERATOR_CAPTURE = new ThreadLocal<>();

    /**
     * Capture the ChunkGenerator before super() runs.
     * Must NOT write to instance fields here — use static ThreadLocal instead.
     */
    @Inject(method = "<init>", at = @At("HEAD"))
    private static void worldpresetpack$captureGenerator(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess storageAccess,
            com.mojang.datafixers.DataFixer dataFixer,
            net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager templateManager,
            java.util.concurrent.Executor executor,
            net.minecraft.util.thread.BlockableEventLoop<java.lang.Runnable> mainThreadExecutor,
            net.minecraft.world.level.chunk.LightChunkGetter lightChunkGetter,
            ChunkGenerator generator,
            net.minecraft.world.level.entity.ChunkStatusUpdateListener chunkStatusUpdateListener,
            java.util.function.Supplier<net.minecraft.world.level.storage.SavedDataStorage> savedDataStorage,
            net.minecraft.world.level.TicketStorage ticketStorage,
            int viewDistance,
            boolean sync,
            CallbackInfo ci) {
        GENERATOR_CAPTURE.set(generator);
    }

    /**
     * After the constructor finishes, clear the ThreadLocal to avoid leaks.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void worldpresetpack$clearCapture(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess storageAccess,
            com.mojang.datafixers.DataFixer dataFixer,
            net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager templateManager,
            java.util.concurrent.Executor executor,
            net.minecraft.util.thread.BlockableEventLoop<java.lang.Runnable> mainThreadExecutor,
            net.minecraft.world.level.chunk.LightChunkGetter lightChunkGetter,
            ChunkGenerator generator,
            net.minecraft.world.level.entity.ChunkStatusUpdateListener chunkStatusUpdateListener,
            java.util.function.Supplier<net.minecraft.world.level.storage.SavedDataStorage> savedDataStorage,
            net.minecraft.world.level.TicketStorage ticketStorage,
            int viewDistance,
            boolean sync,
            CallbackInfo ci) {
        GENERATOR_CAPTURE.remove();
    }

    /**
     * Redirect the dummy() fallback: if our VoidChunkGenerator is in use,
     * return its real noise settings so RandomState builds a proper Climate.Sampler.
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;dummy()Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;"
            )
    )
    private NoiseGeneratorSettings worldpresetpack$redirectDummyNoiseSettings() {
        ChunkGenerator captured = GENERATOR_CAPTURE.get();
        if (captured instanceof VoidChunkGenerator voidGen) {
            return voidGen.generatorSettings().value();
        }
        return NoiseGeneratorSettings.dummy();
    }
}
