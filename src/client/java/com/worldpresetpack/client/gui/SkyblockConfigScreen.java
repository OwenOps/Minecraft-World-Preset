package com.worldpresetpack.client.gui;

import com.worldpresetpack.client.util.BiomeModeApplier;
import com.worldpresetpack.config.SkyblockConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class SkyblockConfigScreen extends Screen {

    private static final int CONTENT_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 4;

    private final Screen parent;
    private final WorldCreationUiState uiState;
    private MultiLineTextWidget descriptionWidget;

    public SkyblockConfigScreen(Screen parent, WorldCreationUiState uiState) {
        super(Component.translatable("worldpresetpack.config.title"));
        this.parent = parent;
        this.uiState = uiState;
    }

    @Override
    protected void init() {
        int startY = (this.height / 2) - 70;
        int centerX = (this.width - CONTENT_WIDTH) / 2;
        int row = 0;

        addRenderableWidget(CycleButton.<SkyblockConfig.Difficulty>builder(
                        value -> Component.translatable("worldpresetpack.difficulty." + value.getSerializedName()),
                        SkyblockConfig.difficulty)
                .withValues(List.of(
                        SkyblockConfig.Difficulty.EASY,
                        SkyblockConfig.Difficulty.CLASSIC,
                        SkyblockConfig.Difficulty.HARD))
                .withTooltip(value -> Tooltip.create(difficultyDescription(value)))
                .create(centerX, startY + row++ * (BUTTON_HEIGHT + SPACING), CONTENT_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.difficulty"),
                        (button, value) -> {
                            SkyblockConfig.difficulty = value;
                            setDescription(difficultyDescription(value));
                            BiomeModeApplier.apply(uiState);
                        }));

        CycleButton<Boolean> structuresButton = CycleButton.<Boolean>builder(
                        value -> Component.translatable(value
                                ? "worldpresetpack.structures.on"
                                : "worldpresetpack.structures.off"),
                        SkyblockConfig.generateStructures)
                .withValues(List.of(Boolean.TRUE, Boolean.FALSE))
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        value ? "worldpresetpack.structures.on.desc" : "worldpresetpack.structures.off.desc")))
                .create(centerX, startY + row++ * (BUTTON_HEIGHT + SPACING), CONTENT_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.structures"),
                        (button, value) -> {
                            SkyblockConfig.generateStructures = value;
                            setDescription(Component.translatable(
                                    value ? "worldpresetpack.structures.on.desc" : "worldpresetpack.structures.off.desc"));
                            BiomeModeApplier.apply(uiState);
                        });
        structuresButton.active = SkyblockConfig.overworldBiome.allowsStructures();
        addRenderableWidget(structuresButton);

        addRenderableWidget(CycleButton.<SkyblockConfig.OverworldBiome>builder(
                        SkyblockConfigScreen::biomeLabel,
                        SkyblockConfig.overworldBiome)
                .withValues(Arrays.asList(SkyblockConfig.OverworldBiome.values()))
                .withTooltip(value -> Tooltip.create(biomeDescription(value)))
                .create(centerX, startY + row++ * (BUTTON_HEIGHT + SPACING), CONTENT_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.biome"),
                        (button, value) -> {
                            SkyblockConfig.overworldBiome = value;
                            if (!value.allowsStructures()) {
                                SkyblockConfig.generateStructures = false;
                                structuresButton.setValue(false);
                                structuresButton.active = false;
                            } else {
                                structuresButton.active = true;
                            }
                            setDescription(biomeDescription(value));
                            BiomeModeApplier.apply(uiState);
                        }));

        int descriptionY = startY + row * (BUTTON_HEIGHT + SPACING) + 8;
        descriptionWidget = new MultiLineTextWidget(
                centerX, descriptionY, difficultyDescription(SkyblockConfig.difficulty), this.font)
                .setMaxWidth(CONTENT_WIDTH);
        descriptionWidget.active = false;
        addRenderableWidget(descriptionWidget);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                btn -> this.onClose()
        ).bounds(centerX, descriptionY + 48, CONTENT_WIDTH, BUTTON_HEIGHT).build());
    }

    private void setDescription(Component text) {
        if (descriptionWidget != null) {
            descriptionWidget.setMessage(text);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    private static Component difficultyDescription(SkyblockConfig.Difficulty difficulty) {
        return Component.translatable("worldpresetpack.difficulty." + difficulty.getSerializedName() + ".desc");
    }

    private static Component biomeLabel(SkyblockConfig.OverworldBiome biome) {
        if (biome.fixedBiome() == null || biome == SkyblockConfig.OverworldBiome.VOID) {
            return Component.translatable("worldpresetpack.biome." + biome.getSerializedName());
        }
        return Component.translatable("biome.minecraft." + biome.getSerializedName());
    }

    private static Component biomeDescription(SkyblockConfig.OverworldBiome biome) {
        if (biome == SkyblockConfig.OverworldBiome.VANILLA || biome == SkyblockConfig.OverworldBiome.VOID) {
            return Component.translatable("worldpresetpack.biome." + biome.getSerializedName() + ".desc");
        }
        return Component.translatable("worldpresetpack.biome.fixed.desc");
    }
}
