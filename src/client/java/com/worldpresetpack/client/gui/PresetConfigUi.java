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
        return entry.preset().is(ModWorldPresets.SKYBLOCK)
                || entry.preset().is(ModWorldPresets.ONE_BLOCK);
    }

    public static Component buttonLabel(WorldCreationUiState.WorldTypeEntry entry) {
        if (entry.preset().is(ModWorldPresets.ONE_BLOCK)) {
            return Component.translatable("worldpresetpack.oneblock.config.button");
        }
        return Component.translatable("worldpresetpack.config.button");
    }

    public static Screen open(Screen parent, WorldCreationUiState uiState) {
        if (uiState.getWorldType().preset().is(ModWorldPresets.SKYBLOCK)) {
            return new SkyblockConfigScreen(parent, uiState);
        }
        if (uiState.getWorldType().preset().is(ModWorldPresets.ONE_BLOCK)) {
            return new OneBlockConfigScreen(parent, uiState);
        }
        return parent;
    }
}
