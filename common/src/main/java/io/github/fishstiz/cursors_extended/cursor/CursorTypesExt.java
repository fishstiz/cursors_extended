package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import org.lwjgl.system.MemoryUtil;

import static com.mojang.blaze3d.platform.cursor.CursorType.createStandardCursor;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_NESW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_NWSE_CURSOR;

public class CursorTypesExt {
    public static final CursorType GRABBING = create("grabbing");
    public static final CursorType SHIFT = create("shift");
    public static final CursorType BUSY = create("busy");
    public static final CursorType RESIZE_NWSE = createStandardCursor(GLFW_RESIZE_NWSE_CURSOR, "resize_nwse", CursorType.DEFAULT);
    public static final CursorType RESIZE_NESW = createStandardCursor(GLFW_RESIZE_NESW_CURSOR, "resize_nesw", CursorType.DEFAULT);
    public static final CursorType GRABBING_HOLD = new HoldType(GRABBING);
    public static final CursorType RESIZE_EW_HOLD = new HoldType(CursorTypes.RESIZE_EW);
    public static final CursorType RESIZE_NS_HOLD = new HoldType(CursorTypes.RESIZE_NS);
    public static final CursorType RESIZE_ALL_HOLD = new HoldType(CursorTypes.RESIZE_ALL);

    private CursorTypesExt() {
    }

    private static CursorType create(String name) {
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
