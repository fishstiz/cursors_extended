package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.util.List;

public class OSAnimatedCursorTexture extends AbstractCursorTexture implements CursorTexture.Animated {
    private final AnimationState animationState;
    private final AnimatedCursorFrame fallback;
    private final AnimatedCursorFrame[] frames;
    private final Identifier texturePath;
    private final CursorMetadata metadata;
    private final float scale;
    private final int xhot;
    private final int yhot;
    private final int textureWidth;
    private final int textureHeight;
    private final byte[] pixels;

    public OSAnimatedCursorTexture(
            NativeImage image,
            Identifier path,
            CursorMetadata metadata,
            CursorProperties settings
    ) throws IOException, OSUnsupportedAnimationException {
        CursorMetadata.Animation animation = metadata.requireAnimation();

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        List<AnimatedCursorFrame> frames = AnimatedCursorFrame.createFrames(animation, image);
        AnimatedCursorFrame fallback = frames.removeFirst();
        long handle = NativeImageUtil.createAnimatedCursor(image, animation.mode(), frames, settings);
        super(handle);

        try {
            this.fallback = fallback;
            this.frames = frames.toArray(AnimatedCursorFrame[]::new);
            this.metadata = metadata;
            this.texturePath = path;
            this.scale = SettingsUtil.sanitizeScale(settings.scale());
            this.xhot = SettingsUtil.sanitizeHotspot(settings.xhot(), imageWidth);
            this.yhot = SettingsUtil.sanitizeHotspot(settings.yhot(), imageHeight);
            this.textureWidth = imageWidth;
            this.textureHeight = imageHeight;
            this.pixels = NativeImageUtil.getBytes(image);
            this.animationState = AnimationState.of(animation.mode(), this.frames.length, this::getFrame);
        } catch (Exception e) {
            if (handle != MemoryUtil.NULL) {
                destroy(handle);
            }
            throw e;
        }
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
    public float scale() {
        return scale;
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
        return currentVirtualFrame().spriteWidth();
    }

    @Override
    public int spriteHeight() {
        return currentVirtualFrame().spriteHeight();
    }

    @Override
    public int spriteVOffset() {
        return currentVirtualFrame().spriteVOffset();
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

    private AnimatedCursorFrame currentVirtualFrame() {
        return getFrame(animationState.currentFrame());
    }

    private AnimatedCursorFrame getFrame(int index) {
        if (frames.length == 0) {
            return fallback;
        }
        if (index < 0 || index >= frames.length) {
            return frames[0];
        }
        return frames[index];
    }

    @Override
    public void restartAnimation() {
        animationState.reset();
    }
}
