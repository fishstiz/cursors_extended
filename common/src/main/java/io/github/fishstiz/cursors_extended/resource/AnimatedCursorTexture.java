package io.github.fishstiz.cursors_extended.resource;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.cursor.AnimationState;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.util.List;

public final class AnimatedCursorTexture implements CursorTexture {
    private final AnimationState animationState;
    private final List<Frame> frames;
    private final Frame baseFrame;
    private final ResourceLocation path;
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
            Boolean animated,
            boolean enabled,
            float scale,
            int xhot,
            int yhot,
            NativeImage image,
            ResourceLocation path,
            CursorMetadata metadata,
            Frame baseFrame,
            List<Frame> frames
    ) throws IOException {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("frames cannot be empty.");
        }

        CursorMetadata.Animation animation = metadata.requireAnimation();

        this.enabled = enabled;
        this.animated = animated == null || animated;
        this.metadata = metadata;
        this.animationState = AnimationState.of(animation.mode());
        this.frames = List.copyOf(animation.mode().isReversed() ? frames.reversed() : frames);
        this.baseFrame = baseFrame;
        this.path = path;
        this.scale = scale;
        this.xhot = xhot;
        this.yhot = yhot;
        this.textureWidth = image.getWidth();
        this.textureHeight = image.getHeight();
        this.pixels = NativeImageUtil.getBytes(image);
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
        return enabled ? currentFrame().texture.handle() : MemoryUtil.NULL;
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
    public int spriteWidth() {
        return currentFrame().texture.spriteWidth();
    }

    @Override
    public int spriteHeight() {
        return currentFrame().texture.spriteHeight();
    }

    @Override
    public int spriteVOffset() {
        Frame frame = currentFrame();
        return frame.index * frame.texture.spriteHeight();
    }

    @Override
    public @NotNull ResourceLocation texturePath() {
        return path;
    }

    @Override
    public CursorMetadata metadata() {
        return metadata;
    }

    @Override
    public void close() {
        frames.forEach(frame -> frame.texture().close());
    }

    public int frameCount() {
        return frames.size();
    }

    public Frame currentFrame() {
        int index = animationState.next(this);
        return getFrame(index);
    }

    public Frame getFrame(int index) {
        if (!animated) {
            return baseFrame;
        }
        if (index < 0 || index >= frames.size()) {
            return frames.getFirst();
        }
        return frames.get(index);
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public void restartAnimation() {
        animationState.reset();
    }

    public record Frame(BasicCursorTexture texture, int index, int time) {
    }
}