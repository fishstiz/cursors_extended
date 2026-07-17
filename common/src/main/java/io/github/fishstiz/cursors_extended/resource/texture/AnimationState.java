package io.github.fishstiz.cursors_extended.resource.texture;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

import java.util.Random;
import java.util.function.IntFunction;

abstract sealed class AnimationState {
    protected static final Random RANDOM = new Random();
    protected static final long MS_PER_TICK = SharedConstants.MILLIS_PER_TICK;
    protected final int frameCount;
    protected final IntFunction<AnimatedCursorFrame> frameGetter;
    private int lastFrame;
    private long lastFrameTime;

    AnimationState(int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
        this.frameCount = frameCount;
        this.frameGetter = frameGetter;
    }

    static AnimationState of(AnimationMode mode, int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
        return switch (mode) {
            case LOOP, LOOP_REVERSE -> new Loop(frameCount, frameGetter);
            case FORWARDS, REVERSE -> new Forwards(frameCount, frameGetter);
            case OSCILLATE -> new Oscillate(frameCount, frameGetter);
            case RANDOM -> new RandomState(frameCount, frameGetter);
            case RANDOM_CYCLE -> new RandomCycle(frameCount, frameGetter);
        };
    }

    private boolean shouldAdvance() {
        AnimatedCursorFrame frame = frameGetter.apply(lastFrame);
        long currentTime = Util.getMillis();
        return currentTime - lastFrameTime >= frame.duration() * MS_PER_TICK;
    }

    protected int lastFrame() {
        return lastFrame;
    }

    protected abstract int nextFrame();

    public final int currentFrame() {
        if (shouldAdvance()) {
            lastFrameTime = Util.getMillis();
            lastFrame = nextFrame();
        }
        return lastFrame;
    }

    public void reset() {
        lastFrameTime = Util.getMillis();
        lastFrame = 0;
    }

    private static final class Loop extends AnimationState {
        Loop(int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
            super(frameCount, frameGetter);
        }

        @Override
        protected int nextFrame() {
            return (lastFrame() + 1) % frameCount;
        }
    }

    private static final class Forwards extends AnimationState {
        Forwards(int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
            super(frameCount, frameGetter);
        }

        @Override
        protected int nextFrame() {
            return Math.min(lastFrame() + 1, frameCount - 1);
        }
    }

    private static final class Oscillate extends AnimationState {
        private boolean reversed;

        Oscillate(int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
            super(frameCount, frameGetter);
        }

        @Override
        protected int nextFrame() {
            int lastFrame = lastFrame();
            reversed = lastFrame != 0 && (lastFrame == frameCount - 1 || reversed);
            return reversed ? lastFrame - 1 : lastFrame + 1;
        }

        @Override
        public void reset() {
            super.reset();
            reversed = false;
        }
    }

    private static final class RandomState extends AnimationState {
        RandomState(int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
            super(frameCount, frameGetter);
        }

        @Override
        protected int nextFrame() {
            int lastFrame = lastFrame();

            if (frameCount > 1) {
                int newFrame;
                do {
                    newFrame = RANDOM.nextInt(frameCount);
                } while (newFrame == lastFrame);
                return newFrame;
            }

            return lastFrame;
        }
    }

    private static final class RandomCycle extends AnimationState {
        private int[] shuffledFrames;
        private int shuffledIndex = 0;

        RandomCycle(int frameCount, IntFunction<AnimatedCursorFrame> frameGetter) {
            super(frameCount, frameGetter);
        }

        @Override
        protected int nextFrame() {
            if (shuffledFrames == null || shuffledIndex >= frameCount) {
                shuffleFrames(frameCount);
            }

            return shuffledFrames[shuffledIndex++];
        }

        private void shuffleFrames(int count) {
            shuffledFrames = new int[count];
            for (int i = 0; i < count; i++) {
                shuffledFrames[i] = i;
            }

            for (int i = count - 1; i > 0; i--) {
                int j = RANDOM.nextInt(i + 1);
                int tmp = shuffledFrames[i];
                shuffledFrames[i] = shuffledFrames[j];
                shuffledFrames[j] = tmp;
            }

            shuffledIndex = 0;
        }

        @Override
        public void reset() {
            super.reset();
            shuffledFrames = null;
            shuffledIndex = 0;
        }
    }
}