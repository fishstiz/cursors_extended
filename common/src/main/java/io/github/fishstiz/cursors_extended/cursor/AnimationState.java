package io.github.fishstiz.cursors_extended.cursor;

import io.github.fishstiz.cursors_extended.resource.AnimatedCursorTexture;
import net.minecraft.Util;

import java.util.Random;

public sealed interface AnimationState {
    int next(AnimatedCursorTexture texture);

    void reset();

    static AnimationState of(AnimationMode mode, int initialFrameIndex) {
        return switch (mode) {
            case LOOP, LOOP_REVERSE -> new Loop(initialFrameIndex);
            case FORWARDS, REVERSE -> new Forwards(initialFrameIndex);
            case OSCILLATE -> new Oscillate(initialFrameIndex);
            case RANDOM, RANDOM_CYCLE -> new RandomCycle(initialFrameIndex);
        };
    }

    abstract sealed class Base implements AnimationState {
        protected int currentFrameIndex;
        protected long lastFrameTime = 0;

        protected Base(int initialFrameIndex) {
            this.currentFrameIndex = initialFrameIndex;
        }

        protected boolean shouldAdvance(AnimatedCursorTexture texture) {
            AnimatedCursorTexture.Frame currentFrame = texture.getFrame(currentFrameIndex);
            long currentTime = Util.getMillis();
            return currentTime - lastFrameTime >= currentFrame.time() * 50L;
        }

        protected void updateFrameTime() {
            lastFrameTime = Util.getMillis();
        }

        @Override
        public void reset() {
            lastFrameTime = Util.getMillis();
            currentFrameIndex = 0;
        }
    }

    final class Loop extends Base {
        public Loop(int initialFrameIndex) {
            super(initialFrameIndex);
        }

        @Override
        public int next(AnimatedCursorTexture texture) {
            if (shouldAdvance(texture)) {
                updateFrameTime();
                currentFrameIndex = (currentFrameIndex + 1) % texture.frameCount();
            }
            return currentFrameIndex;
        }
    }

    final class Forwards extends Base {
        public Forwards(int initialFrameIndex) {
            super(initialFrameIndex);
        }

        @Override
        public int next(AnimatedCursorTexture texture) {
            if (shouldAdvance(texture)) {
                updateFrameTime();
                currentFrameIndex = Math.min(currentFrameIndex + 1, texture.frameCount() - 1);
            }
            return currentFrameIndex;
        }
    }

    final class Oscillate extends Base {
        private boolean reversed;

        public Oscillate(int initialFrameIndex) {
            super(initialFrameIndex);
            this.reversed = initialFrameIndex > 0;
        }

        @Override
        public int next(AnimatedCursorTexture texture) {
            if (shouldAdvance(texture)) {
                updateFrameTime();
                reversed = currentFrameIndex != 0 &&
                           (currentFrameIndex == texture.frameCount() - 1 || reversed);
                currentFrameIndex = reversed ? currentFrameIndex - 1 : currentFrameIndex + 1;
            }
            return currentFrameIndex;
        }

        @Override
        public void reset() {
            super.reset();
            reversed = false;
        }
    }

    final class RandomCycle extends Base {
        private final Random random = new Random();
        private int[] shuffledFrames;
        private int shuffledIndex = 0;

        public RandomCycle(int initialFrameIndex) {
            super(initialFrameIndex);
        }

        @Override
        public int next(AnimatedCursorTexture texture) {
            if (shouldAdvance(texture)) {
                updateFrameTime();
                int count = texture.frameCount();

                if (shuffledFrames == null || shuffledIndex >= count) {
                    shuffleFrames(count);
                }

                currentFrameIndex = shuffledFrames[shuffledIndex++];
            }
            return currentFrameIndex;
        }

        private void shuffleFrames(int count) {
            shuffledFrames = new int[count];
            for (int i = 0; i < count; i++) {
                shuffledFrames[i] = i;
            }

            for (int i = count - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
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