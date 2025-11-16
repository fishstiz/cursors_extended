package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.TexturedCursorType;

public final class ModCursor {
    private final long handle;
    private final String source;
    private final String name;
    private final boolean custom;
    private CursorType cursorType;

    private ModCursor(long handle, String source, String name, boolean custom) {
        this.handle = handle;
        this.source = source;
        this.name = name;
        this.custom = custom;
    }

    public ModCursor(long handle, String source, String name) {
        this(handle, source, name, false);
    }

    public ModCursor(long handle, String source, CursorType cursorType) {
        this(handle, source, cursorType.toString());
        this.cursorType = cursorType;
    }

    public static ModCursor createCustom(long handle, String source) {
        return new ModCursor(handle, source, createCustomName(source, handle), true);
    }

    public long handle() {
        return handle;
    }

    public String source() {
        return source;
    }

    public boolean custom() {
        return custom;
    }

    public CursorType cursorType() {
        if (cursorType == null) {
            cursorType = new CursorType(name, handle);
            ((TexturedCursorType) cursorType).cursors_extended$setCustom(custom);
        }
        return cursorType;
    }

    private static String createCustomName(String source, long handle) {
        return "cursors_extended/custom_cursor/" + source + "/" + handle;
    }
}
