package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import org.jspecify.annotations.Nullable;

public sealed interface CursorStateTracker permits CursorStateTrackerImpl, CursorStateTrackerImpl.DefaultTracker {
    void trackCursor(ModCursor cursor);

    void resetCursor(long window, String source);

    void setCursor(long window, ModCursor cursor);

    default void untrackCursor(ModCursor cursor) {
    }

    default boolean isTracking() {
        return false;
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
            ((WindowCursor) (Object) minecraftWindow).cursors_extended$setCurrentCursor(cursorType);
        }
    }
}
