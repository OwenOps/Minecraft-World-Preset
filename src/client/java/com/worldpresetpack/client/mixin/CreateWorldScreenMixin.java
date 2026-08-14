package com.worldpresetpack.client.mixin;

import com.worldpresetpack.client.gui.SkyblockConfigScreen;
import com.worldpresetpack.client.mixin.accessor.CreateWorldScreenAccessor;
import com.worldpresetpack.registry.ModWorldPresets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Unique private Button worldpresetpack$configButton;
    @Unique private WorldCreationUiState worldpresetpack$uiState;

    @Inject(method = "init", at = @At("TAIL"))
    private void worldpresetpack$onInit(CallbackInfo ci) {
        CreateWorldScreen self = (CreateWorldScreen) (Object) this;
        worldpresetpack$uiState = ((CreateWorldScreenAccessor) self).worldpresetpack$getUiState();

        // "Configure Skyblock" button — bottom center, above Create button
        worldpresetpack$configButton = Button.builder(
                Component.translatable("worldpresetpack.config.button"),
                btn -> Minecraft.getInstance().setScreenAndShow(
                        new SkyblockConfigScreen(self, worldpresetpack$uiState))
        ).bounds(self.width / 2 - 75, self.height - 72, 150, 20).build();

        addRenderableWidget(worldpresetpack$configButton);
        worldpresetpack$updateVisibility(worldpresetpack$uiState);
        worldpresetpack$uiState.addListener(this::worldpresetpack$updateVisibility);
    }

    @Unique
    private void worldpresetpack$updateVisibility(WorldCreationUiState state) {
        if (worldpresetpack$configButton != null) {
            worldpresetpack$configButton.visible =
                    state.getWorldType().preset().is(ModWorldPresets.SKYBLOCK);
        }
    }
}
