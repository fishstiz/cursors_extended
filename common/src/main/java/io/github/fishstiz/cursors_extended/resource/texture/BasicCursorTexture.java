package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;

public final class BasicCursorTexture extends AbstractCursorTexture implements CursorTexture.Stateful {
    private final float scale;
    private final int xhot;
    private final int yhot;
    private final int textureWidth;
    private final int textureHeight;
    private final byte[] pixels;
    private final ResourceLocation texturePath;
    private final CursorMetadata metadata;
    private boolean enabled;

    public BasicCursorTexture(
            NativeImage image,
            ResourceLocation texturePath,
            CursorMetadata metadata,
            CursorProperties settings
    ) throws IOException {
        super(image, settings);
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
        return enabled ? super.handle() : MemoryUtil.NULL;
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
    public CursorMetadata metadata() {
        return metadata;
    }

    @Override
    public @NotNull ResourceLocation texturePath() {
        return texturePath;
    }

    @Override
    public CursorTexture recreate(CursorProperties properties) throws IOException {
        NativeImage image = null;
        try {
            image = NativeImage.read(pixels);
            return new BasicCursorTexture(image, texturePath, metadata, properties);
        } catch (Exception e) {
            if (image != null) {
                image.close();
            }
            throw e;
        }
    }
}