package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;

public abstract class AbstractCursorTexture implements CursorTexture {
    private long handle;

    protected AbstractCursorTexture(NativeImage image, CursorProperties settings) throws IOException {
        this.handle = NativeImageUtil.createCursor(image, settings);
    }

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public void close() {
        if (handle != MemoryUtil.NULL) {
            SDLMouse.SDL_DestroyCursor(handle);
            handle = MemoryUtil.NULL;
        }
    }
}
