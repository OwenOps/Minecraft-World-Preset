package com.worldpresetpack.config;

import net.minecraft.util.StringRepresentable;

/**
 * In-memory create-world options for OneBlock. Reset when the preset is selected.
 * Pace is copied into the world's SavedData on first tick.
 */
public final class OneBlockConfig {

    private OneBlockConfig() {}

    public enum Pace implements StringRepresentable {
        SLOW("slow", 1.5f),
        NORMAL("normal", 1.0f),
        FAST("fast", 0.6f);

        private final String id;
        private final float phaseLengthMultiplier;

        Pace(String id, float phaseLengthMultiplier) {
            this.id = id;
            this.phaseLengthMultiplier = phaseLengthMultiplier;
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        public float phaseLengthMultiplier() {
            return phaseLengthMultiplier;
        }

        public static Pace fromSerialized(String id) {
            for (Pace pace : values()) {
                if (pace.id.equals(id)) {
                    return pace;
                }
            }
            return NORMAL;
        }
    }

    public static Pace pace = Pace.NORMAL;

    public static void reset() {
        pace = Pace.NORMAL;
    }
}
