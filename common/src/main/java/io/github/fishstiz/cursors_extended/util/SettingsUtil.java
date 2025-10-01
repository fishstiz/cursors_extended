package io.github.fishstiz.cursors_extended.util;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.resource.CursorTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

import static io.github.fishstiz.cursors_extended.CursorsExtended.CONFIG;

public class SettingsUtil {
    public static final int IMAGE_SIZE_MIN = 8;
    public static final int IMAGE_SIZE_GUI_MAX = 128;
    public static final float SCALE_AUTO_PREFERRED = 0;
    public static final float SCALE_AUTO_THRESHOLD_MAX = 0.49f;
    public static final float SCALE = 1;
    public static final float SCALE_MIN = 0;
    public static final float SCALE_MAX = 8;
    public static final float SCALE_STEP = 0.05f;
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

    public static boolean isAutoScale(float scale) {
        return scale <= SCALE_AUTO_THRESHOLD_MAX;
    }

    public static boolean isAutoScale(double scale) {
        return isAutoScale((float) scale);
    }

    public static @Nullable Component getAutoText(float scale) {
        return isAutoScale(scale) ? Component.translatable("options.guiScale.auto") : null;
    }

    public static @Nullable Component getAutoText(double scale) {
        return getAutoText((float) scale);
    }

    public static float getAutoScale(float scale) {
        if (isAutoScale(scale)) {
            OptionInstance<Integer> guiScale = Minecraft.getInstance().options.guiScale();
            int max = Integer.MAX_VALUE;
            int guiScaleValue = guiScale.get();

            if (guiScale.values() instanceof OptionInstance.ClampingLazyMaxIntRange range) {
                max = range.maxInclusive();
            }

            return guiScaleValue != 0 ? Math.min(max, guiScaleValue) : Minecraft.getInstance().getWindow().getGuiScale();
        }
        return scale;
    }

    public static float sanitizeScale(float scale) {
        double clampedScale = clamp(scale, SCALE_MIN, SCALE_MAX);
        double mappedScale = Math.round(clampedScale / SCALE_STEP) * SCALE_STEP;

        if (isAutoScale(mappedScale)) {
            return SCALE_AUTO_PREFERRED;
        }

        return Math.round(mappedScale * 100) / 100f;
    }

    public static int sanitizeHotspot(int hotspot, int imageSize) {
        return clamp(hotspot, HOT_MIN, imageSize - 1);
    }

    public static int sanitizeXHot(int xhot, @NotNull Cursor cursor) {
        return sanitizeHotspot(xhot, cursor.getTexture() != null ? cursor.getTexture().spriteWidth() : 0);
    }

    public static int sanitizeYHot(int yhot, @NotNull Cursor cursor) {
        return sanitizeHotspot(yhot, cursor.getTexture() != null ? cursor.getTexture().spriteHeight() : 0);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int getMaxXHot(Cursor cursor) {
        if (cursor != null && cursor.getTexture() != null) {
            return cursor.getTexture().spriteWidth() - 1;
        }
        return 0;
    }

    public static int getMaxYHot(Cursor cursor) {
        if (cursor != null && cursor.getTexture() != null) {
            return cursor.getTexture().spriteHeight() - 1;
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

    public static boolean restoreNonGlobalSettings(@NotNull Cursor cursor) {
        CursorTexture texture = cursor.getTexture();
        if (texture == null) {
            return false;
        }

        CONFIG.putCursorSettings(cursor, ignoreIfGlobal(cursor, texture.metadata().getCursorSettings()));
        CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor)));
        return true;
    }

    public static void restoreCursorSettings() {
        for (Cursor cursor : CursorsExtended.getInstance().getRegistry().getCursors()) {
            CursorTexture texture = cursor.getTexture();
            if (texture != null) {
                Config.CursorSettings settings = CONFIG.getOrCreateSettings(cursor);
                settings.merge(texture.metadata().getCursorSettings());
                CursorsExtended.getInstance().getLoader().updateTexture(cursor, CONFIG.getGlobal().apply(settings));
            }
        }
    }

    private static Config.CursorSettings ignoreIfGlobal(@NotNull Cursor cursor, @NotNull Config.CursorSettings settings) {
        Config.CursorSettings currentSettings = CONFIG.getOrCreateSettings(cursor);
        Config.CursorSettings validated = settings.copy();

        if (CONFIG.getGlobal().isScaleActive()) {
            validated.setScale(currentSettings.getScale());
        }
        if (CONFIG.getGlobal().isXHotActive()) {
            validated.setXHot(cursor, currentSettings.getXHot());
        }
        if (CONFIG.getGlobal().isYHotActive()) {
            validated.setYHot(cursor, currentSettings.getYHot());
        }
        return validated;
    }

    public static <T> T getOrDefault(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
