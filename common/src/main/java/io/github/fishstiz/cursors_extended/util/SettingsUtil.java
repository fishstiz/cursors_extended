package io.github.fishstiz.cursors_extended.util;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

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
    public static final Boolean ANIMATED = null;

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

    public static float getAutoScale(float scale) {
        if (isAutoScale(scale)) {
            OptionInstance<@NonNull Integer> guiScale = Minecraft.getInstance().options.guiScale();
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

    public static int sanitizeXHot(int xhot, @NonNull Cursor cursor) {
        return sanitizeHotspot(xhot, cursor.getTexture() != null ? cursor.getTexture().spriteWidth() : 0);
    }

    public static int sanitizeYHot(int yhot, @NonNull Cursor cursor) {
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

    public static int getFrameWidth(@Nullable Integer specifiedWidth, int imageWidth, int imageHeight) {
        int preferredFrameSize = Math.min(imageWidth, imageHeight);
        return Math.min(Math.abs(Objects.requireNonNullElse(specifiedWidth, preferredFrameSize)), imageWidth);
    }

    public static int getFrameHeight(@Nullable Integer specifiedHeight, int imageWidth, int imageHeight) {
        int preferredFrameSize = Math.min(imageWidth, imageHeight);
        return Math.min(Math.abs(Objects.requireNonNullElse(specifiedHeight, preferredFrameSize)), imageHeight);
    }

    public static boolean equalSettings(@Nullable CursorProperties a, @Nullable CursorProperties b, boolean excludeGlobal) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a != null && b != null) {
            boolean equal = a.enabled() == b.enabled() && Objects.equals(a.animated(), b.animated());

            if (!excludeGlobal || !CursorsExtended.CONFIG.getGlobal().isXHotActive()) {
                equal &= a.xhot() == b.xhot();
            }
            if (!excludeGlobal || !CursorsExtended.CONFIG.getGlobal().isYHotActive()) {
                equal &= a.yhot() == b.yhot();
            }
            if (!excludeGlobal || !CursorsExtended.CONFIG.getGlobal().isScaleActive()) {
                equal &= Float.compare(a.scale(), b.scale()) == 0;
            }

            return equal;
        }
        return false;
    }
}
