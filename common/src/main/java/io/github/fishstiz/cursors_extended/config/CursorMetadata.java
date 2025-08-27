package io.github.fishstiz.cursors_extended.config;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@SuppressWarnings({"unused", "FieldMayBeFinal"})
public final class CursorMetadata implements Serializable {
    public static final String FILE_TYPE = ".json";
    private Config.CursorSettings cursor = new Config.CursorSettings();
    private AnimationData animation;

    public Config.CursorSettings getCursorSettings() {
        return this.cursor;
    }

    public @Nullable AnimationData getAnimation() {
        return this.animation;
    }
}
