package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

public final class BasicCursorTexture extends AbstractCursorTexture {
    private final float scale;
    private final int xhot;
    private final int yhot;
    private final int textureWidth;
    private final int textureHeight;
    private final int spriteWidth;
    private final int spriteHeight;
    private final byte[] pixels;
    private final Identifier texturePath;
    private final CursorMetadata metadata;

    public BasicCursorTexture(
            long handle,
            byte[] pixels,
            int imageWidth,
            int imageHeight,
            int spriteWidth,
            int spriteHeight,
            Identifier texturePath,
            CursorMetadata metadata,
            CursorProperties settings
    ) {
        super(handle);
        this.scale = SettingsUtil.sanitizeScale(settings.scale());
        this.xhot = SettingsUtil.sanitizeHotspot(settings.xhot(), imageWidth);
        this.yhot = SettingsUtil.sanitizeHotspot(settings.yhot(), imageHeight);
        this.textureWidth = imageWidth;
        this.textureHeight = imageHeight;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.texturePath = texturePath;
        this.metadata = metadata;
        this.pixels = pixels;
    }

    public BasicCursorTexture(
            NativeImage image,
            Identifier texturePath,
            CursorMetadata metadata,
            CursorProperties settings
    ) throws IOException {
        byte[] pixels = NativeImageUtil.getBytes(image);
        long handle = NativeImageUtil.createCursor(image, settings);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        this(handle, pixels, imageWidth, imageHeight, imageWidth, imageHeight, texturePath, metadata, settings);
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
    public int spriteWidth() {
        return spriteWidth;
    }

    @Override
    public int spriteHeight() {
        return spriteHeight;
    }

    @Override
    public CursorMetadata metadata() {
        return metadata;
    }

    @Override
    public @NonNull Identifier texturePath() {
        return texturePath;
    }

    @Override
    public NativeImage toNativeImage() throws IOException {
        return NativeImage.read(pixels);
    }
}