package io.github.fishstiz.cursors_extended.resource.texture;

import io.github.fishstiz.cursors_extended.cursor.AnimationMode;
import net.minecraft.util.Util;

import java.util.Random;

public sealed interface AnimationState {
    int next(AnimatedCursorTexture texture);

    void reset();

    static AnimationState of(AnimationMode mode) {
        return switch (mode) {
            case LOOP, LOOP_REVERSE -> new Loop();
            case FORWARDS, REVERSE -> new Forwards();
            case OSCILLATE -> new Oscillate();
            case RANDOM -> new RandomState();
            case RANDOM_CYCLE -> new RandomCycle();
        };
    }

    abstract sealed class Base implements AnimationState {
        protected static final long MS_PER_TICK = 50;
        protected int currentFrameIndex;
        protected long lastFrameTime;

        protected boolean shouldAdvance(AnimatedCursorTexture texture) {
            AnimatedCursorTexture.Frame currentFrame = texture.getFrame(currentFrameIndex);
            long currentTime = Util.getMillis();
            return currentTime - lastFrameTime >= currentFrame.time() * MS_PER_TICK;
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

    final class RandomState extends Base {
        private final Random random = new Random();

        @Override
        public int next(AnimatedCursorTexture texture) {
            if (shouldAdvance(texture)) {
                updateFrameTime();

                int count = texture.frameCount();
                if (count > 1) {
                    int newFrame;
                    do {
                        newFrame = random.nextInt(count);
                    } while (newFrame == currentFrameIndex);
                    currentFrameIndex = newFrame;
                }
            }
            return currentFrameIndex;
        }
    }

    final class RandomCycle extends Base {
        private final Random random = new Random();
        private int[] shuffledFrames;
        private int shuffledIndex = 0;

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