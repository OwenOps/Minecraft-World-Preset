package com.worldpresetpack.client;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

/**
 * Bridge statique pour la communication entre Mixins client.
 * Évite les champs non-privés dans les Mixins.
 */
public final class MixinBridge {

    private MixinBridge() {}

    public interface PresetChangeListener {
        void onPresetChanged(WorldCreationUiState state);
    }

    private static PresetChangeListener listener;

    public static void setListener(PresetChangeListener l) {
        listener = l;
    }

    public static void notifyPresetChanged(WorldCreationUiState state) {
        if (listener != null) {
            listener.onPresetChanged(state);
        }
    }
}
