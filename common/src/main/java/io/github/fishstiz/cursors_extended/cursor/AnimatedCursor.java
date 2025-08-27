package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.AnimationData;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AnimatedCursor extends Cursor {
    private AnimationMode mode = AnimationMode.LOOP;
    private Map<Integer, FrameCursor> cursors = new HashMap<>();
    private List<FrameData> frames = new ArrayList<>();
    private boolean animated = true;
    private FrameData fallbackFrame;

    AnimatedCursor(CursorType type, Consumer<Cursor> onLoad) {
        super(type, onLoad);
    }

    void loadImage(NativeImage image, Config.CursorSettings settings, CursorMetadata metadata, AnimationData animation) throws IOException {
        super.loadImage(image, settings, metadata);

        int availableFrames = image.getHeight() / this.getTextureWidth();

        Map<Integer, FrameCursor> newCursors = createCursors(image, settings, metadata, availableFrames);
        List<FrameData> newFrames = createFrames(animation, newCursors, availableFrames);

        updateState(settings.isAnimated(), animation, newCursors, newFrames);
    }

    private HashMap<Integer, FrameCursor> createCursors(NativeImage image, Config.CursorSettings settings, CursorMetadata metadata, int availableFrames) throws IOException {
        HashMap<Integer, FrameCursor> newCursors = new HashMap<>();
        for (int i = 1; i < availableFrames; i++) {
            newCursors.put(i, createCursor(image, settings, metadata, i));
        }
        return newCursors;
    }

    private List<FrameData> createFrames(AnimationData animation, Map<Integer, FrameCursor> cursors, int availableFrames) {
        List<FrameData> newFrames = new ArrayList<>();

        if (animation.getFrames().isEmpty()) {
            newFrames.add(new FrameData(this, animation.getFrametime()));
            for (int i = 1; i < availableFrames; i++) {
                newFrames.add(new FrameData(cursors.get(i), animation.getFrametime()));
            }
            return newFrames;
        }

        for (AnimationData.Frame frame : animation.getFrames()) {
            int index = frame.getIndex();
            if (index < 0 || index >= availableFrames) {
                CursorsExtended.LOGGER.warn("[cursors-extended] Sprite does not exist on index {} for cursor type '{}', skipping frame.", index, getType());
                continue;
            }
            newFrames.add(new FrameData(index == 0 ? this : cursors.get(index), frame.getTime(animation)));
        }
        return newFrames;
    }

    private FrameCursor createCursor(NativeImage image, Config.CursorSettings settings, CursorMetadata metadata, int index) throws IOException {
        FrameCursor cursor = new FrameCursor(index);
        int size = this.getTextureWidth();
        try (NativeImage cropped = NativeImageUtil.cropImage(image, 0, index * size, size, size)) {
            cursor.loadImage(cropped, settings, metadata);
        }
        return cursor;
    }

    private void updateState(Boolean animated, AnimationData animation, Map<Integer, FrameCursor> newCursors, List<FrameData> newFrames) {
        this.setAnimated(animated);
        this.fallbackFrame = new FrameData(this, 1);
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

    public FrameData getFrame(int index) {
        try {
            FrameData frame = frames.get(index);
            if (!isAnimated() || frame.cursor() == null || !frame.cursor().isEnabled()) {
                return getFallbackFrame();
            }
            return frame;
        } catch (IndexOutOfBoundsException e) {
            return getFallbackFrame();
        }
    }

    public FrameData nextFrame(AnimationState state) {
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

    public FrameData getFallbackFrame() {
        if (this.fallbackFrame == null) {
            this.fallbackFrame = new FrameData(this, 1);
        }
        return this.fallbackFrame;
    }

    public record FrameData(Cursor cursor, int time) {
    }

    private class FrameCursor extends Cursor {
        private final int textureIndex;

        private FrameCursor(int textureIndex) {
            super(AnimatedCursor.this);
            this.textureIndex = textureIndex;
        }

        @Override
        public int getTextureIndex() {
            return this.textureIndex;
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
