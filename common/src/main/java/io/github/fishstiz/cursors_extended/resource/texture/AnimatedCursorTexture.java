package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.cursor.AnimationState;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.util.List;

public final class AnimatedCursorTexture implements CursorTexture.Stateful {
    private final AnimationState animationState;
    private final Frame fallback;
    private final Frame[] frames;
    private final ResourceLocation texturePath;
    private final CursorMetadata metadata;
    private final float scale;
    private final int xhot;
    private final int yhot;
    private final int textureWidth;
    private final int textureHeight;
    private final byte[] pixels;
    private boolean enabled;
    private boolean animated;

    public AnimatedCursorTexture(
            AnimationState animationState,
            NativeImage image,
            ResourceLocation path,
            CursorMetadata metadata,
            CursorProperties settings
    ) throws IOException {
        CursorMetadata.Animation animation = metadata.requireAnimation();

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int preferredFrameSize = Math.min(imageWidth, imageHeight);
        int frameWidth = Math.min(Math.abs(SettingsUtil.getOrDefault(animation.width(), preferredFrameSize)), imageWidth);
        int frameHeight = Math.min(Math.abs(SettingsUtil.getOrDefault(animation.height(), preferredFrameSize)), imageHeight);

        SettingsUtil.assertImageSize(frameWidth, frameHeight);
        int availableFrames = imageHeight / frameHeight;

        List<Sprite> textures = new ObjectArrayList<>(availableFrames);

        try {
            for (int i = 0; i < availableFrames; i++) {
                int yOffset = i * frameHeight;
                try (NativeImage croppedImage = NativeImageUtil.cropImage(image, 0, yOffset, frameWidth, frameHeight)) {
                    textures.add(new Sprite(yOffset, croppedImage, settings));
                }
            }

            this.fallback = new Frame(textures.getFirst(), animation.frametime());
            this.frames = createAnimationFrames(animation, textures, availableFrames).toArray(Frame[]::new);
        } catch (Exception e) {
            textures.forEach(CursorTexture::close);
            throw e;
        }

        this.enabled = settings.enabled();
        this.animated = settings.animated() == null || settings.animated();
        this.metadata = metadata;
        this.animationState = animationState;
        this.texturePath = path;
        this.scale = settings.scale();
        this.xhot = settings.xhot();
        this.yhot = settings.yhot();
        this.textureWidth = imageWidth;
        this.textureHeight = imageHeight;
        this.pixels = NativeImageUtil.getBytes(image);
    }

    private static List<Frame> createAnimationFrames(CursorMetadata.Animation animation, List<Sprite> textures, int availableFrames) {
        List<Frame> frames = new ObjectArrayList<>();

        if (animation.frames().isEmpty()) {
            for (int i = 0; i < availableFrames; i++) {
                frames.add(new Frame(textures.get(i), animation.frametime()));
            }
            return frames;
        }

        for (CursorMetadata.Animation.Frame frame : animation.frames()) {
            int index = frame.index();
            if (index < 0 || index >= availableFrames) {
                CursorsExtended.LOGGER.warn("[cursors_extended] Sprite does not exist on index {}.", index);
                continue;
            }
            frames.add(new Frame(textures.get(index), frame.clampedTime(animation)));
        }

        return frames;
    }


    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void toggle() {
        this.enabled = !enabled;
        restartAnimation();
    }

    @Override
    public long handle() {
        return enabled() ? currentFrame().sprite.handle() : MemoryUtil.NULL;
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
    public @NotNull Boolean animated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
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
        return currentFrame().sprite.spriteWidth();
    }

    @Override
    public int spriteHeight() {
        return currentFrame().sprite.spriteHeight();
    }

    @Override
    public int spriteVOffset() {
        return currentFrame().sprite.spriteVOffset();
    }

    @Override
    public @NotNull ResourceLocation texturePath() {
        return texturePath;
    }

    @Override
    public CursorMetadata metadata() {
        return metadata;
    }

    @Override
    public void free() {
        fallback.sprite.free();
        for (Frame frame : frames) {
            frame.sprite.free();
        }
    }

    public int frameCount() {
        return frames.length;
    }

    public Frame currentFrame() {
        return getFrame(animationState.next(this));
    }

    public Frame getFrame(int index) {
        if (!animated || !enabled || frames.length == 0) {
            return fallback;
        }
        if (index < 0 || index >= frames.length) {
            return frames[0];
        }
        return frames[index];
    }

    public void restartAnimation() {
        animationState.reset();
    }

    @Override
    public CursorTexture recreate(CursorProperties properties) throws IOException {
        NativeImage image = null;
        try {
            image = NativeImage.read(pixels);
            return new AnimatedCursorTexture(animationState, image, texturePath, metadata, properties);
        } catch (Exception e) {
            if (image != null) {
                image.close();
            }
            throw e;
        }
    }

    public final class Sprite extends AbstractCursorTexture {
        private final int textureWidth;
        private final int textureHeight;
        private final int spriteVOffset;

        public Sprite(int vOffset, NativeImage image, CursorProperties properties) throws IOException {
            super(image, properties);
            this.spriteVOffset = vOffset;
            this.textureWidth = image.getWidth();
            this.textureHeight = image.getHeight();
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
        public int spriteVOffset() {
            return spriteVOffset;
        }

        @Override
        public @NotNull ResourceLocation texturePath() {
            return texturePath;
        }

        @Override
        public CursorMetadata metadata() {
            return metadata;
        }
    }

    public record Frame(Sprite sprite, int time) {
    }
}