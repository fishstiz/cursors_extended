package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import org.lwjgl.system.MemoryUtil;

public class CursorTypesExt {
    public static final CursorType GRABBING = createDummy("grabbing");
    public static final CursorType SHIFT = createDummy("shift");
    public static final CursorType BUSY = createDummy("busy");
    public static final CursorType RESIZE_NWSE = createDummy("resize_nwse");
    public static final CursorType RESIZE_NESW = createDummy("resize_nesw");
    public static final CursorType GRABBING_HOLD = new HoldType(GRABBING);
    public static final CursorType RESIZE_EW_HOLD = new HoldType(CursorTypes.RESIZE_EW);
    public static final CursorType RESIZE_NS_HOLD = new HoldType(CursorTypes.RESIZE_NS);
    public static final CursorType RESIZE_ALL_HOLD = new HoldType(CursorTypes.RESIZE_ALL);

    private CursorTypesExt() {
    }

    private static CursorType createDummy(String name) {
        return new CursorType(name, MemoryUtil.NULL);
    }

    public static boolean isHoldType(CursorType cursorType) {
        return cursorType instanceof HoldType && CursorsExtended.CONFIG.isHeldCursorsEnabled();
    }

    private static final class HoldType extends CursorType {
        public HoldType(CursorType type) {
            super(type.toString(), type.handle);
        }
    }
}
