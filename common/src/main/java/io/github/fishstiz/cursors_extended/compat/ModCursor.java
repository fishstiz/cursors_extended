package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;

public record ModCursor(long handle, String source, CursorType cursorType, boolean custom) {
    public ModCursor(long handle, String source, CursorType cursorType) {
        this(handle, source, cursorType, false);
    }

    public static ModCursor ofUnknownType(long handle, String source) {
        return new ModCursor(handle, source, new CursorType("cursors_extended/unknown_cursor/" + handle, handle), true);
    }
}
