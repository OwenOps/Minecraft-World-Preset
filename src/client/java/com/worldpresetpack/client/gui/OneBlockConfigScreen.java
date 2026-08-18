package com.worldpresetpack.client.gui;

import com.worldpresetpack.client.util.OneBlockApplier;
import com.worldpresetpack.config.OneBlockConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OneBlockConfigScreen extends Screen {

    private static final int CONTENT_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;
    private final WorldCreationUiState uiState;
    private MultiLineTextWidget descriptionWidget;

    public OneBlockConfigScreen(Screen parent, WorldCreationUiState uiState) {
        super(Component.translatable("worldpresetpack.oneblock.config.title"));
        this.parent = parent;
        this.uiState = uiState;
    }

    @Override
    protected void init() {
        int startY = (this.height / 2) - 40;
        int centerX = (this.width - CONTENT_WIDTH) / 2;

        addRenderableWidget(CycleButton.<OneBlockConfig.Pace>builder(
                        value -> Component.translatable("worldpresetpack.oneblock.pace." + value.getSerializedName()),
                        OneBlockConfig.pace)
                .withValues(List.of(
                        OneBlockConfig.Pace.SLOW,
                        OneBlockConfig.Pace.NORMAL,
                        OneBlockConfig.Pace.FAST))
                .withTooltip(value -> Tooltip.create(paceDescription(value)))
                .create(centerX, startY, CONTENT_WIDTH, BUTTON_HEIGHT,
                        Component.translatable("worldpresetpack.oneblock.pace"),
                        (button, value) -> {
                            OneBlockConfig.pace = value;
                            setDescription(paceDescription(value));
                            OneBlockApplier.apply(uiState);
                        }));

        int descriptionY = startY + BUTTON_HEIGHT + 12;
        descriptionWidget = new MultiLineTextWidget(
                centerX, descriptionY, paceDescription(OneBlockConfig.pace), this.font)
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

    private static Component paceDescription(OneBlockConfig.Pace pace) {
        return Component.translatable("worldpresetpack.oneblock.pace." + pace.getSerializedName() + ".desc");
    }
}
