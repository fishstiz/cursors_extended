package io.github.fishstiz.cursors_extended.resource;

import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

public sealed interface CursorTexture extends ClientAsset.Texture, AutoCloseable permits BasicCursorTexture, AnimatedCursorTexture {
    boolean enabled();

    void toggle();

    long handle();

    float scale();

    int xhot();

    int yhot();

    int textureWidth();

    int textureHeight();

    byte[] pixels();

    CursorMetadata metadata();

    @Override
    default @NotNull ResourceLocation id() {
        return texturePath();
    }

    default int spriteWidth() {
        return textureWidth();
    }

    default int spriteHeight() {
        return textureHeight();
    }

    default int spriteVOffset() {
        return 0;
    }

    default void setEnabled(boolean enabled) {
        if (enabled != enabled()) {
            toggle();
        }
    }

    @Override
    default void close() {
        if (handle() != MemoryUtil.NULL) {
            GLFW.glfwDestroyCursor(handle());
        }
    }
}
