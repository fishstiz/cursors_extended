package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public final class Cursor {
    private final CursorType cursorType;
    private boolean lazy = true;

    public Cursor(CursorType cursorType) {
        this.cursorType = cursorType;
    }

    public long handle() {
        if (!isCustom() && CursorsExtended.CONFIG.getOrCreateSettings(this).enabled()) {
            CursorTexture texture = getTexture();
            if (texture != null && texture.handle() != MemoryUtil.NULL) {
                return texture.handle();
            }
        }
        return cursorType.handle;
    }

    public String name() {
        return cursorType.toString();
    }

    public @Nullable CursorTexture getTexture() {
        return texturedCursorType().cursors_extended$getTexture();
    }

    public void setTexture(CursorTexture texture) {
        texturedCursorType().cursors_extended$setTexture(texture);
    }

    public boolean hasTexture() {
        return getTexture() != null;
    }

    public boolean isTextureEnabled() {
        return hasTexture() && CursorsExtended.CONFIG.getOrCreateSettings(this).enabled();
    }

    public CursorType cursorType() {
        return cursorType;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isCustom() {
        return texturedCursorType().cursors_extended$isCustom();
    }

    public Component text() {
        return Component.translatable("cursors_extended.options.cursor-type." + name());
    }

    private TexturedCursorType texturedCursorType() {
        return (TexturedCursorType) cursorType;
    }
}
