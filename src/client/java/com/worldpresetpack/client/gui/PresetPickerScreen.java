package com.worldpresetpack.client.gui;

import com.worldpresetpack.client.registry.ModPresetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

public class PresetPickerScreen extends Screen {

    private static final int CONTENT_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 4;

    private final Screen parent;
    private final WorldCreationUiState uiState;

    public PresetPickerScreen(Screen parent, WorldCreationUiState uiState) {
        super(Component.translatable("worldpresetpack.presets.title"));
        this.parent = parent;
        this.uiState = uiState;
    }

    @Override
    protected void init() {
        int centerX = (this.width - CONTENT_WIDTH) / 2;
        int rowY = 48;

        for (ModPresetRegistry.Definition definition : ModPresetRegistry.all()) {
            var entryOpt = ModPresetRegistry.toWorldTypeEntry(this.uiState, definition.key());
            if (entryOpt.isEmpty()) {
                continue;
            }
            var entry = entryOpt.get();
            int presetRowY = rowY;

            Component name = entry.describePreset();
            if (ModPresetRegistry.isSelected(this.uiState, definition)) {
                name = Component.translatable("worldpresetpack.presets.selected", name);
            }

            addRenderableWidget(Button.builder(
                    name,
                    btn -> this.selectPreset(definition)
            ).bounds(centerX, presetRowY, CONTENT_WIDTH, BUTTON_HEIGHT).build());

            rowY += BUTTON_HEIGHT + SPACING;

            MultiLineTextWidget description = new MultiLineTextWidget(
                    centerX,
                    rowY,
                    Component.translatable(definition.descriptionKey()),
                    this.font)
                    .setMaxWidth(CONTENT_WIDTH);
            description.active = false;
            addRenderableWidget(description);

            rowY += description.getHeight() + 12;
        }

        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                btn -> this.onClose()
        ).bounds(centerX, this.height - 36, CONTENT_WIDTH, BUTTON_HEIGHT).build());
    }

    private void selectPreset(ModPresetRegistry.Definition definition) {
        ModPresetRegistry.select(this.uiState, definition);
        this.onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }
}
