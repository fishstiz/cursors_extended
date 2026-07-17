package io.github.fishstiz.cursors_extended.resource.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.util.List;

public record AnimatedCursorFrame(
        int textureWidth,
        int textureHeight,
        int spriteWidth,
        int spriteHeight,
        int spriteVOffset,
        int duration
) implements CursorSprite {
    public AnimatedCursorFrame(CursorSprite sprite, int duration) {
        this(sprite.textureWidth(), sprite.textureHeight(), sprite.spriteWidth(), sprite.spriteHeight(), sprite.spriteVOffset(), duration);
    }

    static List<AnimatedCursorFrame> createFrames(CursorMetadata.Animation animation, NativeImage image) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int frameWidth = SettingsUtil.getFrameWidth(animation.width(), imageWidth, imageHeight);
        int frameHeight = SettingsUtil.getFrameHeight(animation.height(), imageWidth, imageHeight);

        SettingsUtil.assertImageSize(frameWidth, frameHeight);
        int availableFrames = imageHeight / frameHeight;

        List<AnimatedCursorFrame> sprites = new ObjectArrayList<>(availableFrames);
        List<AnimatedCursorFrame> frames = new ObjectArrayList<>(availableFrames + 1);

        for (int i = 0; i < availableFrames; i++) {
            sprites.add(new AnimatedCursorFrame(
                    imageWidth,
                    imageHeight,
                    frameWidth,
                    frameHeight,
                    i * frameHeight,
                    animation.frametime()
            ));
        }

        if (animation.frames().isEmpty()) {
            frames.addAll(sprites);
            return finalizeFrames(animation, frames, sprites.getFirst());
        }

        for (CursorMetadata.Animation.Frame frameMetadata : animation.frames()) {
            int index = frameMetadata.index();
            if (index < 0 || index >= availableFrames) {
                CursorsExtended.LOGGER.warn("[cursors_extended] Sprite does not exist on index {}.", index);
                continue;
            }
            frames.add(new AnimatedCursorFrame(sprites.get(index), frameMetadata.clampedTime(animation)));
        }

        return finalizeFrames(animation, frames, sprites.getFirst());
    }

    private static List<AnimatedCursorFrame> finalizeFrames(
            CursorMetadata.Animation animation,
            List<AnimatedCursorFrame> frames,
            AnimatedCursorFrame fallback
    ) {
        List<AnimatedCursorFrame> finalFrames = animation.mode().reverse() ? new ObjectArrayList<>(frames.reversed()) : frames;
        finalFrames.addFirst(fallback);
        return finalFrames;
    }
}
