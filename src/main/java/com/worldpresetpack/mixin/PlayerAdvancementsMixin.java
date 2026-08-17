package com.worldpresetpack.mixin;

import com.worldpresetpack.WorldPresetPackMod;
import com.worldpresetpack.worldgen.SkyblockWorlds;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private void worldpresetpack$skyblockOnly(
            AdvancementHolder holder, String criterion, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        if (!WorldPresetPackMod.MOD_ID.equals(holder.id().getNamespace())) return;
        if (SkyblockWorlds.isSkyblock(player.level().getServer())) return;
        cir.setReturnValue(false);
    }
}
