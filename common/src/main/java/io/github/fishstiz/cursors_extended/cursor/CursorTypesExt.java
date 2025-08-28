package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.mixin.CursorTypeAccess;

public class CursorTypesExt {
    public static final CursorType GRABBING = create("grabbing");
    public static final CursorType GRABBING_HOLD = create(GRABBING.toString());
    public static final CursorType SHIFT = create("shift");
    public static final CursorType BUSY = create("busy");
    public static final CursorType RESIZE_ALL_HOLD = create(CursorTypes.RESIZE_ALL.toString());
    public static final CursorType RESIZE_EW_HOLD = create(CursorTypes.RESIZE_EW.toString());
    public static final CursorType RESIZE_NWSE = create("resize_nwse");
    public static final CursorType RESIZE_NESW = create("resize_nesw");

    private CursorTypesExt() {
    }

    private static CursorType create(String name) {
        return CursorTypeAccess.cursors_extended$createCursorType(name, 0L);
    }

    public static boolean isHoldType(CursorType cursorType) {
        return cursorType == GRABBING_HOLD || cursorType == RESIZE_ALL_HOLD || cursorType == RESIZE_EW_HOLD;
    }
}
