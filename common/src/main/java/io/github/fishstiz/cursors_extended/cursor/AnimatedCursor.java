package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.util.SettingsUtil.assertImageSize;
import static io.github.fishstiz.cursors_extended.util.SettingsUtil.getOrDefault;

public class AnimatedCursor extends Cursor {
    private AnimationMode mode = AnimationMode.LOOP;
    private Map<Integer, FrameCursor> cursors = new HashMap<>();
    private List<Frame> frames = new ArrayList<>();
    private boolean animated = true;
    private Frame fallbackFrame;
    private int totalTextureWidth;
    private int totalTextureHeight;
    private int frameWidth;
    private int frameHeight;

    AnimatedCursor(CursorType type, Consumer<Cursor> onLoad) {
        super(type, onLoad);
    }

    void loadImage(NativeImage image, Config.CursorSettings settings, CursorMetadata metadata, CursorMetadata.Animation animation) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int preferredFrameSize = Math.min(imageWidth, imageHeight);

        try {
            this.frameWidth = Math.min(Math.abs(getOrDefault(animation.getWidth(), preferredFrameSize)), imageWidth);
            this.frameHeight = Math.min(Math.abs(getOrDefault(animation.getHeight(), preferredFrameSize)), imageHeight);
            assertImageSize(frameWidth, frameHeight);

            try (NativeImage croppedImage = NativeImageUtil.cropImage(image, 0, 0, this.frameWidth, this.frameHeight)) {
                super.loadImage(croppedImage, settings, metadata);
                this.totalTextureWidth = imageWidth;
                this.totalTextureHeight = imageHeight;
            }

            int availableFrames = image.getHeight() / this.frameHeight;
            Map<Integer, FrameCursor> newCursors = createCursors(image, settings, metadata, availableFrames);
            List<Frame> newFrames = createFrames(animation, newCursors, availableFrames);

            updateState(settings.isAnimated(), animation, newCursors, newFrames);
        } catch (Exception e) {
            this.destroy();
            throw e;
        }
    }

    private HashMap<Integer, FrameCursor> createCursors(NativeImage image, Config.CursorSettings settings, CursorMetadata metadata, int availableFrames) throws IOException {
        HashMap<Integer, FrameCursor> newCursors = new HashMap<>();
        for (int i = 1; i < availableFrames; i++) {
            newCursors.put(i, createCursor(image, settings, metadata, i));
        }
        return newCursors;
    }

    private List<Frame> createFrames(CursorMetadata.Animation animation, Map<Integer, FrameCursor> cursors, int availableFrames) {
        List<Frame> newFrames = new ArrayList<>();

        if (animation.getFrames().isEmpty()) {
            newFrames.add(new Frame(this, animation.getFrametime()));
            for (int i = 1; i < availableFrames; i++) {
                newFrames.add(new Frame(cursors.get(i), animation.getFrametime()));
            }
            return newFrames;
        }

        for (CursorMetadata.Animation.Frame frame : animation.getFrames()) {
            int index = frame.getIndex();
            if (index < 0 || index >= availableFrames) {
                CursorsExtended.LOGGER.warn("[cursors-extended] Sprite does not exist on index {} for cursor type '{}', skipping frame.", index, getType());
                continue;
            }
            newFrames.add(new Frame(index == 0 ? this : cursors.get(index), frame.getTime(animation)));
        }
        return newFrames;
    }

    private FrameCursor createCursor(NativeImage image, Config.CursorSettings settings, CursorMetadata metadata, int index) throws IOException {
        FrameCursor cursor = new FrameCursor(index);
        try (NativeImage cropped = NativeImageUtil.cropImage(image, 0, index * this.frameHeight, this.frameWidth, this.frameHeight)) {
            cursor.loadImage(cropped, settings, metadata);
        }
        return cursor;
    }

    private void updateState(Boolean animated, CursorMetadata.Animation animation, Map<Integer, FrameCursor> newCursors, List<Frame> newFrames) {
        this.setAnimated(animated);
        this.fallbackFrame = new Frame(this, 1);
        this.mode = animation.mode;
        this.frames = this.mode.isReversed() ? newFrames.reversed() : newFrames;

        List<Cursor> oldCursors = List.copyOf(this.cursors.values());
        this.cursors = newCursors;
        oldCursors.forEach(Cursor::destroy);
    }

    @Override
    protected void updateImage(double scale, int xhot, int yhot) {
        super.updateImage(scale, xhot, yhot);
        applyToFrames(cursor -> cursor.updateImage(scale, xhot, yhot));
    }

    private void applyToFrames(Consumer<Cursor> action) {
        for (Cursor cursor : cursors.values()) {
            action.accept(cursor);
        }
    }

    public int getFrameCount() {
        return Math.max(frames.size(), 1);
    }

    public Frame getFrame(int index) {
        try {
            Frame frame = frames.get(index);
            if (!isAnimated() || frame.cursor() == null || !frame.cursor().isEnabled()) {
                return getFallbackFrame();
            }
            return frame;
        } catch (IndexOutOfBoundsException e) {
            return getFallbackFrame();
        }
    }

    public Frame nextFrame(AnimationState state) {
        return this.getFrame(state.next(this));
    }

    public boolean isAnimated() {
        return this.animated;
    }

    public void setAnimated(Boolean animated) {
        this.animated = animated == null || animated;
    }

    public AnimationMode getMode() {
        return this.mode;
    }

    @Override
    public void apply(Config.CursorSettings settings) {
        this.setAnimated(settings.isAnimated());
        super.apply(settings);
    }

    @Override
    public void enable(boolean enabled) {
        super.enable(enabled);
        applyToFrames(cursor -> cursor.enable(enabled));
    }

    @Override
    public void destroy() {
        super.destroy();
        applyToFrames(Cursor::destroy);
    }

    @Override
    public void reload() {
        super.reload();
        applyToFrames(Cursor::reload);
    }

    public Frame getFallbackFrame() {
        if (this.fallbackFrame == null) {
            this.fallbackFrame = new Frame(this, 1);
        }
        return this.fallbackFrame;
    }

    @Override
    public int getSpriteWidth() {
        return this.frameWidth;
    }

    @Override
    public int getSpriteHeight() {
        return this.frameHeight;
    }

    @Override
    public int getTextureWidth() {
        this.assertLoaded();
        return this.totalTextureWidth;
    }

    @Override
    public int getTextureHeight() {
        this.assertLoaded();
        return this.totalTextureHeight;
    }

    public record Frame(Cursor cursor, int time) {
    }

    private class FrameCursor extends Cursor {
        private final int spriteIndex;

        private FrameCursor(int spriteIndex) {
            super(AnimatedCursor.this);
            this.spriteIndex = spriteIndex;
        }

        @Override
        public int getSpriteWidth() {
            return AnimatedCursor.this.getSpriteWidth();
        }

        @Override
        public int getSpriteHeight() {
            return AnimatedCursor.this.getSpriteHeight();
        }

        @Override
        public int getSpriteIndex() {
            return this.spriteIndex;
        }

        @Override
        public int getTextureWidth() throws IllegalStateException {
            return AnimatedCursor.this.getTextureWidth();
        }

        @Override
        public int getTextureHeight() throws IllegalStateException {
            return AnimatedCursor.this.getTextureHeight();
        }
    }
}
