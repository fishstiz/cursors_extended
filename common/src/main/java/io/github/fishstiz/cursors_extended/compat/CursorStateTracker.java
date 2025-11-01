package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import org.jetbrains.annotations.Nullable;

public interface CursorStateTracker {
    CursorStateTracker DEFAULT = new CursorStateTracker() {
    };

    default void trackCursor(ModCursor cursor) {
        CursorStateTrackerImpl.get().trackCursor(cursor);
    }

    default void untrackCursor(ModCursor cursor) {
    }

    default boolean isTracking() {
        return false;
    }

    default void resetCursor(long window, String source) {
        CursorStateTrackerImpl.get().resetCursor(window, source);
    }

    default void setCursor(long window, ModCursor cursor) {
        CursorStateTrackerImpl.get().setCursor(window, cursor);
    }

    default @Nullable ModCursor getCursor(long handle) {
        return null;
    }

    default CursorType getCurrentCursor(long window) {
        return CursorType.DEFAULT;
    }

    static CursorStateTracker get() {
        return CursorStateTrackerImpl.getOrDefault();
    }

    static StackWalker getStackWalker() {
        return CursorStateTrackerImpl.getStackWalker();
    }

    static void syncWithMinecraft(long window, CursorType cursorType) {
        Window minecraftWindow = CursorsExtended.getInstance().getDisplay().getWindow();
        if (window == minecraftWindow.handle()) {
            minecraftWindow.currentCursor = cursorType;
        }
    }
}
