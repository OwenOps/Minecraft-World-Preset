package com.worldpresetpack.client.mixin;

import com.worldpresetpack.client.gui.PresetConfigUi;
import com.worldpresetpack.client.gui.PresetPickerScreen;
import com.worldpresetpack.client.mixin.accessor.CreateWorldScreenAccessor;
import com.worldpresetpack.client.mixin.accessor.GridLayoutAccessor;
import com.worldpresetpack.client.mixin.accessor.GridLayoutChildAccessor;
import com.worldpresetpack.client.mixin.accessor.GridLayoutTabAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class CreateWorldScreenWorldTabMixin {

    /** Inserted under vanilla World Type / Customize (row 0). */
    private static final int PRESETS_ROW = 1;
    private static final int BUTTON_WIDTH = 150;

    @Unique private Button worldpresetpack$presetsButton;
    @Unique private Button worldpresetpack$configButton;
    @Unique private WorldCreationUiState worldpresetpack$uiState;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void worldpresetpack$addPresetButtons(CreateWorldScreen screen, CallbackInfo ci) {
        this.worldpresetpack$uiState = ((CreateWorldScreenAccessor) screen).worldpresetpack$getUiState();

        this.worldpresetpack$presetsButton = Button.builder(
                PresetConfigUi.presetsButtonLabel(this.worldpresetpack$uiState),
                btn -> Minecraft.getInstance().setScreenAndShow(
                        new PresetPickerScreen(screen, this.worldpresetpack$uiState))
        ).width(BUTTON_WIDTH).build();

        this.worldpresetpack$configButton = Button.builder(
                PresetConfigUi.buttonLabel(this.worldpresetpack$uiState.getWorldType()),
                btn -> Minecraft.getInstance().setScreenAndShow(
                        PresetConfigUi.open(screen, this.worldpresetpack$uiState))
        ).width(BUTTON_WIDTH).build();

        GridLayout grid = ((GridLayoutTabAccessor) this).worldpresetpack$getLayout();
        for (Object child : ((GridLayoutAccessor) grid).worldpresetpack$getChildren()) {
            GridLayoutChildAccessor cell = (GridLayoutChildAccessor) child;
            if (cell.worldpresetpack$getRow() >= PRESETS_ROW) {
                cell.worldpresetpack$setRow(cell.worldpresetpack$getRow() + 1);
            }
        }
        grid.addChild(this.worldpresetpack$presetsButton, PRESETS_ROW, 0);
        grid.addChild(this.worldpresetpack$configButton, PRESETS_ROW, 1);

        this.worldpresetpack$updateButtons(this.worldpresetpack$uiState);
        this.worldpresetpack$uiState.addListener(this::worldpresetpack$updateButtons);
    }

    @Unique
    private void worldpresetpack$updateButtons(WorldCreationUiState state) {
        if (this.worldpresetpack$presetsButton != null) {
            this.worldpresetpack$presetsButton.setMessage(PresetConfigUi.presetsButtonLabel(state));
        }
        if (this.worldpresetpack$configButton != null) {
            boolean visible = PresetConfigUi.isConfigurable(state.getWorldType());
            this.worldpresetpack$configButton.visible = visible;
            this.worldpresetpack$configButton.active = visible;
            if (visible) {
                this.worldpresetpack$configButton.setMessage(PresetConfigUi.buttonLabel(state.getWorldType()));
            }
        }
    }
}
