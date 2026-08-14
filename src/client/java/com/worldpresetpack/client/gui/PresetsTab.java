package com.worldpresetpack.client.gui;

import com.worldpresetpack.client.util.BiomeModeApplier;
import com.worldpresetpack.config.SkyblockConfig;
import com.worldpresetpack.registry.ModWorldPresets;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A dedicated tab for Skyblock preset options (Structures, Biome Mode).
 */
public class PresetsTab extends GridLayoutTab {

    private static final int BUTTON_WIDTH = 210;
    private static final int BUTTON_HEIGHT = 20;
    private static final int COLUMN_SPACING = 4;

    private final CycleButton<Boolean> structuresButton;
    private final CycleButton<SkyblockConfig.BiomeMode> biomeModeButton;

    public PresetsTab(WorldCreationUiState uiState) {
        super(Component.translatable("worldpresetpack.tab.presets"));

        // Set up a single-column grid with spacing
        GridLayout.RowHelper rows = this.layout.createRowHelper(1);
        rows.defaultCellSetting().paddingTop(COLUMN_SPACING);

        // Structures toggle button
        this.structuresButton = CycleButton.<Boolean>builder(
                        value -> Component.translatable(value
                                ? "worldpresetpack.structures.on"
                                : "worldpresetpack.structures.off"),
                        SkyblockConfig.generateStructures)
                .withValues(List.of(Boolean.TRUE, Boolean.FALSE))
                .create(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.structures"),
                        (button, value) -> {
                            SkyblockConfig.generateStructures = value;
                            BiomeModeApplier.apply(uiState, SkyblockConfig.biomeMode);
                        });

        // Biome mode cycle button
        this.biomeModeButton = CycleButton.<SkyblockConfig.BiomeMode>builder(
                        mode -> Component.translatable("worldpresetpack.biome." + mode.name().toLowerCase()),
                        SkyblockConfig.biomeMode)
                .withValues(List.of(SkyblockConfig.BiomeMode.VOID, SkyblockConfig.BiomeMode.STANDARD))
                .create(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.biome"),
                        (button, value) -> {
                            SkyblockConfig.biomeMode = value;
                            if (value == SkyblockConfig.BiomeMode.STANDARD) {
                                SkyblockConfig.generateStructures = true;
                                structuresButton.setValue(true);
                            } else {
                                SkyblockConfig.generateStructures = false;
                                structuresButton.setValue(false);
                            }
                            BiomeModeApplier.apply(uiState, value);
                        });

        rows.addChild(structuresButton, LayoutSettings.defaults().alignHorizontallyCenter());
        rows.addChild(biomeModeButton, LayoutSettings.defaults().alignHorizontallyCenter());

        // Sync button states with current preset selection
        boolean isSkyblock = uiState.getWorldType().preset().is(ModWorldPresets.SKYBLOCK);
        structuresButton.active = isSkyblock;
        biomeModeButton.active = isSkyblock;

        // Listen for preset changes to update active state
        uiState.addListener(state -> {
            boolean skyblock = state.getWorldType().preset().is(ModWorldPresets.SKYBLOCK);
            structuresButton.active = skyblock;
            biomeModeButton.active = skyblock;
        });
    }
}
