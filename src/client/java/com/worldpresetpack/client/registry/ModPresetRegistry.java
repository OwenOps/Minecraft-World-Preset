package com.worldpresetpack.client.registry;

import com.worldpresetpack.client.gui.OneBlockConfigScreen;
import com.worldpresetpack.client.gui.SkyblockConfigScreen;
import com.worldpresetpack.client.util.BiomeModeApplier;
import com.worldpresetpack.client.util.OneBlockApplier;
import com.worldpresetpack.config.OneBlockConfig;
import com.worldpresetpack.config.SkyblockConfig;
import com.worldpresetpack.registry.ModWorldPresets;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Single registry for mod world presets shown in the create-world picker.
 * Add one entry here when shipping a new preset.
 */
public final class ModPresetRegistry {

    @FunctionalInterface
    public interface ConfigScreenFactory extends BiFunction<Screen, WorldCreationUiState, Screen> {}

    public record Definition(
            ResourceKey<WorldPreset> key,
            String descriptionKey,
            boolean configurable,
            ConfigScreenFactory configScreen,
            Consumer<WorldCreationUiState> onSelect
    ) {}

    private static final List<Definition> PRESETS = List.of(
            new Definition(
                    ModWorldPresets.SKYBLOCK,
                    "worldpresetpack.presets.skyblock.desc",
                    true,
                    SkyblockConfigScreen::new,
                    uiState -> {
                        SkyblockConfig.reset();
                        BiomeModeApplier.apply(uiState);
                    }),
            new Definition(
                    ModWorldPresets.ONE_BLOCK,
                    "worldpresetpack.presets.oneblock.desc",
                    true,
                    OneBlockConfigScreen::new,
                    uiState -> {
                        OneBlockConfig.reset();
                        OneBlockApplier.apply(uiState);
                    })
    );

    private ModPresetRegistry() {}

    public static List<Definition> all() {
        return PRESETS;
    }

    public static boolean isModPreset(WorldCreationUiState.WorldTypeEntry entry) {
        return find(entry).isPresent();
    }

    public static Optional<Definition> find(WorldCreationUiState.WorldTypeEntry entry) {
        return entry.preset()
                .unwrapKey()
                .flatMap(key -> PRESETS.stream().filter(def -> def.key().equals(key)).findFirst());
    }

    public static Optional<Definition> find(ResourceKey<WorldPreset> key) {
        return PRESETS.stream().filter(def -> def.key().equals(key)).findFirst();
    }

    public static Optional<WorldCreationUiState.WorldTypeEntry> toWorldTypeEntry(
            WorldCreationUiState uiState,
            ResourceKey<WorldPreset> key
    ) {
        return uiState.getSettings()
                .worldgenLoadContext()
                .lookupOrThrow(Registries.WORLD_PRESET)
                .get(key)
                .map(WorldCreationUiState.WorldTypeEntry::new);
    }

    public static void select(WorldCreationUiState uiState, Definition definition) {
        toWorldTypeEntry(uiState, definition.key()).ifPresent(uiState::setWorldType);
    }

    public static boolean isSelected(WorldCreationUiState uiState, Definition definition) {
        return uiState.getWorldType().preset().is(definition.key());
    }
}
