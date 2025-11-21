package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.util.Util;

class ModCursorState {
    private CursorType current;
    private boolean custom;
    private long timestamp;

    ModCursorState(CursorType cursorType, boolean custom) {
        this.timestamp = Util.getMillis();
        this.current = cursorType;
        this.custom = custom;
    }

    void update(CursorType cursorType, boolean custom) {
        if (!CursorTypeUtil.nameEquals(this.current, cursorType)) {
            this.timestamp = Util.getMillis();
        }
        this.current = cursorType;
        this.custom = custom;
    }

    CursorType getCursorType() {
        return this.current;
    }

    long getTimestamp() {
        return this.timestamp;
    }

    public boolean isCustom() {
        return this.custom;
    }
}
