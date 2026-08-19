package com.worldpresetpack.client.gui;

import com.worldpresetpack.client.registry.ModPresetRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

/**
 * Maps a selected world preset to its configure screen.
 */
public final class PresetConfigUi {

    private PresetConfigUi() {}

    public static boolean isConfigurable(WorldCreationUiState.WorldTypeEntry entry) {
        return ModPresetRegistry.find(entry)
                .map(ModPresetRegistry.Definition::configurable)
                .orElse(false);
    }

    public static Component buttonLabel(WorldCreationUiState.WorldTypeEntry entry) {
        return Component.translatable("worldpresetpack.config.button");
    }

    public static Screen open(Screen parent, WorldCreationUiState uiState) {
        return ModPresetRegistry.find(uiState.getWorldType())
                .map(def -> def.configScreen().apply(parent, uiState))
                .orElse(parent);
    }

    public static Component presetsButtonLabel(WorldCreationUiState uiState) {
        return ModPresetRegistry.find(uiState.getWorldType())
                .map(def -> uiState.getWorldType().describePreset())
                .map(name -> Component.translatable("worldpresetpack.presets.button.selected", name))
                .orElse(Component.translatable("worldpresetpack.presets.button"));
    }
}
