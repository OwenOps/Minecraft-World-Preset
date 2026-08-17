package com.worldpresetpack.client.gui;

import com.worldpresetpack.client.util.BiomeModeApplier;
import com.worldpresetpack.config.SkyblockConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SkyblockConfigScreen extends Screen {

    private static final int CONTENT_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 4;

    private final Screen parent;
    private final WorldCreationUiState uiState;

    public SkyblockConfigScreen(Screen parent, WorldCreationUiState uiState) {
        super(Component.translatable("worldpresetpack.config.title"));
        this.parent = parent;
        this.uiState = uiState;
    }

    @Override
    protected void init() {
        int startY = (this.height / 2) - 40;
        int centerX = (this.width - CONTENT_WIDTH) / 2;

        CycleButton<Boolean> structuresButton = CycleButton.<Boolean>builder(
                        value -> Component.translatable(value
                                ? "worldpresetpack.structures.on"
                                : "worldpresetpack.structures.off"),
                        SkyblockConfig.generateStructures)
                .withValues(List.of(Boolean.TRUE, Boolean.FALSE))
                .create(centerX, startY, CONTENT_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.structures"),
                        (button, value) -> {
                            SkyblockConfig.generateStructures = value;
                            BiomeModeApplier.apply(uiState, SkyblockConfig.biomeMode);
                        });
        structuresButton.active = SkyblockConfig.biomeMode == SkyblockConfig.BiomeMode.STANDARD;
        addRenderableWidget(structuresButton);

        CycleButton<SkyblockConfig.BiomeMode> biomeModeButton = CycleButton.<SkyblockConfig.BiomeMode>builder(
                        mode -> Component.translatable("worldpresetpack.biome." + mode.name().toLowerCase()),
                        SkyblockConfig.biomeMode)
                .withValues(List.of(SkyblockConfig.BiomeMode.VOID, SkyblockConfig.BiomeMode.STANDARD))
                .create(centerX, startY + BUTTON_HEIGHT + SPACING, CONTENT_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.biome"),
                        (button, value) -> {
                            SkyblockConfig.biomeMode = value;
                            if (value == SkyblockConfig.BiomeMode.VOID) {
                                SkyblockConfig.generateStructures = false;
                                structuresButton.setValue(false);
                                structuresButton.active = false;
                            } else {
                                structuresButton.active = true;
                            }
                            BiomeModeApplier.apply(uiState, value);
                        });
        addRenderableWidget(biomeModeButton);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                btn -> this.onClose()
        ).bounds(centerX, startY + (BUTTON_HEIGHT + SPACING) * 2 + 10, CONTENT_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }
}
