package io.github.fishstiz.cursors_extended.util;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class SettingsUtil {
    public static final int IMAGE_SIZE_MIN = 8;
    public static final int IMAGE_SIZE_GUI_MAX = 128;
    public static final double SCALE_AUTO_PREFERRED = 0;
    public static final double SCALE_AUTO_THRESHOLD_MAX = 0.49;
    public static final double SCALE = 1.0;
    public static final double SCALE_MIN = 0;
    public static final double SCALE_MAX = 8.0;
    public static final double SCALE_STEP = 0.05;
    public static final int X_HOT = 0;
    public static final int Y_HOT = 0;
    public static final int HOT_MIN = 0;
    public static final int HOT_STEP = 1;
    public static final boolean ENABLED = true;

    private SettingsUtil() {
    }

    public static void assertImageSize(int imageWidth, int imageHeight) throws IOException {
        if (imageWidth < IMAGE_SIZE_MIN || imageHeight < IMAGE_SIZE_MIN) {
            throw new IOException("Image width/height cannot be less than " + IMAGE_SIZE_MIN);
        }
    }

    public static boolean isAutoScale(double scale) {
        return scale <= SCALE_AUTO_THRESHOLD_MAX;
    }

    public static @Nullable Component getAutoText(double scale) {
        return isAutoScale(scale) ? Component.translatable("options.guiScale.auto") : null;
    }

    public static double getAutoScale(double scale) {
        return isAutoScale(scale) ? Minecraft.getInstance().getWindow().getGuiScale() : scale;
    }

    public static double sanitizeScale(double scale) {
        double clampedScale = clamp(scale, SCALE_MIN, SCALE_MAX);
        double mappedScale = Math.round(clampedScale / SCALE_STEP) * SCALE_STEP;

        if (isAutoScale(mappedScale)) {
            return SCALE_AUTO_PREFERRED;
        }

        return (double) Math.round(mappedScale * 100) / 100;
    }

    public static int sanitizeHotspot(int hotspot, int imageSize) {
        return clamp(hotspot, HOT_MIN, imageSize - 1);
    }

    public static int sanitizeXHot(int xhot, @NotNull Cursor cursor) {
        return sanitizeHotspot(xhot, cursor.isLoaded() ? cursor.getSpriteWidth() : 0);
    }

    public static int sanitizeYHot(int yhot, @NotNull Cursor cursor) {
        return sanitizeHotspot(yhot, cursor.isLoaded() ? cursor.getSpriteHeight() : 0);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int getMaxXHot(Cursor cursor) {
        if (cursor != null && cursor.isLoaded()) {
            return cursor.getSpriteWidth() - 1;
        }
        return 0;
    }

    public static int getMaxYHot(Cursor cursor) {
        if (cursor != null && cursor.isLoaded()) {
            return cursor.getSpriteHeight() - 1;
        }
        return 0;
    }

    public static boolean equalSettings(@Nullable Config.CursorSettings a, @Nullable Config.CursorSettings b, boolean excludeGlobal) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a != null && b != null) {
            boolean equal = a.isEnabled() == b.isEnabled() &&
                            Objects.equals(a.isAnimated(), b.isAnimated());

            if (!excludeGlobal || !CursorsExtended.CONFIG.getGlobal().isXHotActive()) {
                equal &= a.getXHot() == b.getXHot();
            }
            if (!excludeGlobal || !CursorsExtended.CONFIG.getGlobal().isYHotActive()) {
                equal &= a.getYHot() == b.getYHot();
            }
            if (!excludeGlobal || !CursorsExtended.CONFIG.getGlobal().isScaleActive()) {
                equal &= Double.compare(a.getScale(), b.getScale()) == 0;
            }

            return equal;
        }
        return false;
    }

    public static <T> T getOrDefault(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
