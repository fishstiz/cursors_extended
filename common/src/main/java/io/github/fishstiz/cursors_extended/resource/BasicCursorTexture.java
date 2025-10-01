package io.github.fishstiz.cursors_extended.resource;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
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
            long handle,
            NativeImage image,
            ResourceLocation texturePath,
            CursorMetadata metadata,
            CursorProperties settings
    ) throws IOException {
        this.handle = handle;
        this.enabled = settings.enabled();
        this.scale = settings.scale();
        this.xhot = settings.xhot();
        this.yhot = settings.yhot();
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
    public Boolean animated() {
        return false;
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