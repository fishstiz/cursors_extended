package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

public interface CursorTexture extends CursorSprite, ClientAsset.Texture, AutoCloseable {
    long handle();

    int xhot();

    int yhot();

    float scale();

    CursorMetadata metadata();

    @Override
    void close();

    @Override
    default @NonNull Identifier id() {
        return texturePath();
    }

    default NativeImage toNativeImage() throws IOException {
        throw new UnsupportedOperationException("Cannot convert CursorTexture to NativeImage");
    }

    interface Animated {
        void restartAnimation();
    }
}
