package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.util.List;

public class SWAnimatedCursorTexture implements CursorTexture, CursorTexture.Animated {
    private final AnimationState animationState;
    private final FrameContainer fallback;
    private final FrameContainer[] frames;
    private final Identifier texturePath;
    private final CursorMetadata metadata;
    private final float scale;
    private final int xhot;
    private final int yhot;
    private final int textureWidth;
    private final int textureHeight;
    private final byte[] pixels;

    public SWAnimatedCursorTexture(
            NativeImage image,
            Identifier path,
            CursorMetadata metadata,
            CursorProperties settings
    ) throws IOException {
        CursorMetadata.Animation animation = metadata.requireAnimation();
        Int2ObjectMap<FrameTexture> frameTextures = new Int2ObjectOpenHashMap<>();

        try {
            List<AnimatedCursorFrame> baseFrames = AnimatedCursorFrame.createFrames(animation, image);
            AnimatedCursorFrame fallback = baseFrames.removeFirst();

            try (NativeImage fallbackSprite = NativeImageUtil.cropImage(image, 0, 0, fallback.spriteWidth(), fallback.spriteHeight())) {
                FrameTexture fallbackTexture = new FrameTexture(NativeImageUtil.createCursor(fallbackSprite, settings));
                frameTextures.put(0, fallbackTexture);
            }

            FrameContainer[] frames = new FrameContainer[baseFrames.size()];
            for (int i = 0; i < baseFrames.size(); i++) {
                AnimatedCursorFrame baseFrame = baseFrames.get(i);
                int spriteVOffset = baseFrame.spriteVOffset();
                FrameTexture frameTexture = frameTextures.get(spriteVOffset);

                if (frameTexture == null) {
                    int frameWidth = baseFrame.spriteWidth();
                    int frameHeight = baseFrame.spriteHeight();

                    try (NativeImage sprite = NativeImageUtil.cropImage(image, 0, spriteVOffset, frameWidth, frameHeight)) {
                        frameTexture = new FrameTexture(NativeImageUtil.createCursor(sprite, settings));
                        frameTextures.put(spriteVOffset, frameTexture);
                    }
                }

                frames[i] = new FrameContainer(baseFrame, frameTexture);
            }

            this.fallback = new FrameContainer(fallback, frameTextures.get(0));
            this.frames = frames;
            this.metadata = metadata;
            this.texturePath = path;
            this.scale = SettingsUtil.sanitizeScale(settings.scale());
            this.xhot = SettingsUtil.sanitizeHotspot(settings.xhot(), image.getWidth());
            this.yhot = SettingsUtil.sanitizeHotspot(settings.yhot(), image.getHeight());
            this.textureWidth = image.getWidth();
            this.textureHeight = image.getHeight();
            this.pixels = NativeImageUtil.getBytes(image);
            this.animationState = AnimationState.of(animation.mode(), this.frames.length, this::getFrame);
        } catch (Exception e) {
            for (FrameTexture frameTexture : frameTextures.values()) {
                frameTexture.close();
            }
            throw e;
        }
    }


    @Override
    public long handle() {
        return currentFrameContainer().texture.handle();
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
        return currentFrameContainer().frame().spriteWidth();
    }

    @Override
    public int spriteHeight() {
        return currentFrameContainer().frame().spriteHeight();
    }

    @Override
    public int spriteVOffset() {
        return currentFrameContainer().frame().spriteVOffset();
    }

    @Override
    public @NonNull Identifier texturePath() {
        return texturePath;
    }

    @Override
    public CursorMetadata metadata() {
        return metadata;
    }

    @Override
    public void close() {
        fallback.texture.close();
        for (FrameContainer frameContainer : frames) {
            frameContainer.texture.close();
        }
    }

    private FrameContainer currentFrameContainer() {
        return getFrameContainer(animationState.currentFrame());
    }

    private FrameContainer getFrameContainer(int index) {
        if (frames.length == 0) {
            return fallback;
        }
        if (index < 0 || index >= frames.length) {
            return frames[0];
        }
        return frames[index];
    }

    private AnimatedCursorFrame getFrame(int index) {
        return getFrameContainer(index).frame();
    }

    @Override
    public void restartAnimation() {
        animationState.reset();
    }

    @Override
    public NativeImage toNativeImage() throws IOException {
        return NativeImage.read(pixels);
    }

    private static final class FrameTexture implements AutoCloseable {
        private long handle;

        private FrameTexture(long handle) {
            this.handle = handle;
        }

        @Override
        public void close() {
            AbstractCursorTexture.destroy(handle);
            this.handle = MemoryUtil.NULL;
        }

        public long handle() {
            return handle;
        }
    }

    private record FrameContainer(AnimatedCursorFrame frame, FrameTexture texture) {
    }
}