package com.worldpresetpack.client.mixin;

import com.worldpresetpack.client.MixinBridge;
import com.worldpresetpack.config.SkyblockConfig;
import com.worldpresetpack.registry.ModWorldPresets;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {

    @Inject(method = "setWorldType", at = @At("TAIL"))
    private void worldpresetpack$onSetWorldType(WorldCreationUiState.WorldTypeEntry entry, CallbackInfo ci) {
        boolean isSkyblock = entry.preset().is(ModWorldPresets.SKYBLOCK);

        if (isSkyblock) {
            SkyblockConfig.reset();
        }

        MixinBridge.notifyPresetChanged((WorldCreationUiState) (Object) this);
    }
}
