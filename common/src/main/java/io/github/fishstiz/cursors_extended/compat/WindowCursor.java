package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;

public interface WindowCursor {
    default void cursors_extended$setCurrentCursor(CursorType cursorType) {
    }
}
