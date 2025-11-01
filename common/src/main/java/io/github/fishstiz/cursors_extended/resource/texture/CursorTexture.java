package io.github.fishstiz.cursors_extended.resource.texture;

import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface CursorTexture extends ClientAsset.Texture, AutoCloseable {
    long handle();

    int xhot();

    int yhot();

    float scale();

    int textureWidth();

    int textureHeight();

    CursorMetadata metadata();

    @Override
    void close();

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

    default CursorTexture recreate(CursorProperties properties) throws IOException {
        throw new IOException("This cursor texture cannot be recreated");
    }
}
