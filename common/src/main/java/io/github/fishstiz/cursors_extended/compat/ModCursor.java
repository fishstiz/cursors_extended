package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;

public record ModCursor(long handle, String source, CursorType cursorType, boolean custom) {
    public ModCursor(long handle, String source, CursorType cursorType) {
        this(handle, source, cursorType, false);
    }

    public static ModCursor ofUnknownType(long handle, String source) {
        return new ModCursor(handle, source, new UnknownType(source, handle), true);
    }

    public static boolean isUnknown(CursorType cursorType) {
        return cursorType instanceof UnknownType;
    }

    private static final class UnknownType extends CursorType {
        public UnknownType(String source, long handle) {
            super("cursors_extended/unknown_cursor/" + source + "/" + handle, handle);
        }
    }
}
