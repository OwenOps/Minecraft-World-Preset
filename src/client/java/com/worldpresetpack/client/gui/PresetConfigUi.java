package com.worldpresetpack.client.gui;

import com.worldpresetpack.registry.ModWorldPresets;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

/**
 * Maps a selected world preset to its configure screen.
 * Add a branch here when a new preset needs its own options.
 */
public final class PresetConfigUi {

    private PresetConfigUi() {}

    public static boolean isConfigurable(WorldCreationUiState.WorldTypeEntry entry) {
        return entry.preset().is(ModWorldPresets.SKYBLOCK);
    }

    public static Component buttonLabel(WorldCreationUiState.WorldTypeEntry entry) {
        // Future: return a per-preset label if several configurable presets exist.
        return Component.translatable("worldpresetpack.config.button");
    }

    public static Screen open(Screen parent, WorldCreationUiState uiState) {
        if (uiState.getWorldType().preset().is(ModWorldPresets.SKYBLOCK)) {
            return new SkyblockConfigScreen(parent, uiState);
        }
        return parent;
    }
}
