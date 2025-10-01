package io.github.fishstiz.cursors_extended.resource;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;

public final class BasicCursorTexture implements CursorTexture {
    private final float scale;
    private final int xhot;
    private final int yhot;
    private final int textureWidth;
    private final int textureHeight;
    private final byte[] pixels;
    private final ResourceLocation texturePath;
    private final CursorMetadata metadata;
    private boolean enabled;
    private long handle;

    public BasicCursorTexture(
            boolean enabled,
            long handle,
            float scale,
            int xhot,
            int yhot,
            NativeImage image,
            ResourceLocation texturePath,
            CursorMetadata metadata
    ) throws IOException {
        this.enabled = enabled;
        this.handle = handle;
        this.scale = scale;
        this.xhot = xhot;
        this.yhot = yhot;
        this.textureWidth = image.getWidth();
        this.textureHeight = image.getHeight();
        this.pixels = NativeImageUtil.getBytes(image);
        this.texturePath = texturePath;
        this.metadata = metadata;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void toggle() {
        this.enabled = !enabled;
    }

    @Override
    public long handle() {
        return enabled ? handle : MemoryUtil.NULL;
    }

    @Override
    public float scale() {
        return scale;
    }

    @Override
    public int xhot() {
        return xhot;
    }

    @Override
    public int yhot() {
        return yhot;
    }

    @Override
    public int textureWidth() {
        return textureWidth;
    }

    @Override
    public int textureHeight() {
        return textureHeight;
    }

    @Override
    public byte[] pixels() {
        return pixels;
    }

    @Override
    public CursorMetadata metadata() {
        return metadata;
    }

    @Override
    public @NotNull ResourceLocation texturePath() {
        return texturePath;
    }

    @Override
    public void close() {
        CursorTexture.super.close();
        this.handle = MemoryUtil.NULL;
    }
}