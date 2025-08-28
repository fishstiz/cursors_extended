package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import org.lwjgl.system.MemoryUtil;

public class CursorTypesExt {
    public static final CursorType GRABBING = create("grabbing");
    public static final CursorType GRABBING_HOLD = asHold(GRABBING);
    public static final CursorType SHIFT = create("shift");
    public static final CursorType BUSY = create("busy");
    public static final CursorType RESIZE_EW_HOLD = asHold(CursorTypes.RESIZE_EW);
    public static final CursorType RESIZE_NS_HOLD = asHold(CursorTypes.RESIZE_NS);
    public static final CursorType RESIZE_ALL_HOLD = asHold(CursorTypes.RESIZE_ALL);
    public static final CursorType RESIZE_NWSE = create("resize_nwse");
    public static final CursorType RESIZE_NESW = create("resize_nesw");

    private CursorTypesExt() {
    }

    private static CursorType create(String name) {
        return new CursorType(name, MemoryUtil.NULL);
    }

    private static CursorType asHold(CursorType type) {
        return new HoldType(type.toString());
    }

    public static boolean isHoldType(CursorType cursorType) {
        return cursorType instanceof HoldType && CursorsExtended.CONFIG.isHeldCursorsEnabled();
    }

    private static final class HoldType extends CursorType {
        public HoldType(String name) {
            super(name, MemoryUtil.NULL);
        }
    }
}
