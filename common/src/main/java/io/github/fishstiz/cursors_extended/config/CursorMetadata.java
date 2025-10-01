package io.github.fishstiz.cursors_extended.config;

import com.google.gson.*;
import io.github.fishstiz.cursors_extended.cursor.AnimationMode;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.fishstiz.cursors_extended.util.SettingsUtil.*;

public record CursorMetadata(CursorSettings cursor, Animation animation) implements Serializable {
    public static final String FILE_TYPE = ".json";

    public CursorMetadata() {
        this(new CursorSettings(), new Animation());
    }

    public Animation requireAnimation() {
        return Objects.requireNonNull(animation, "Metadata animation must not be null");
    }

    public record CursorSettings(
            boolean enabled,
            float scale,
            int xhot,
            int yhot,
            @Nullable Boolean animated
    ) implements SharedCursorSettings<CursorSettings> {
        public CursorSettings() {
            this(ENABLED, SCALE, X_HOT, Y_HOT, ANIMATED);
        }

        @Override
        public CursorSettings copy() {
            return new CursorSettings(enabled, scale, xhot, yhot, animated);
        }
    }

    public record Animation(
            AnimationMode mode,
            int frametime,
            List<Frame> frames,
            @Nullable Integer width,
            @Nullable Integer height
    ) {
        private static final int MIN_TIME = 1;

        public Animation {
            mode = mode != null ? mode : AnimationMode.LOOP;
            frametime = Math.max(frametime, MIN_TIME);
            frames = frames != null ? frames : Collections.emptyList();
        }

        public Animation() {
            this(AnimationMode.LOOP, MIN_TIME, Collections.emptyList(), null, null);
        }

        @Override
        public List<Frame> frames() {
            return List.copyOf(frames);
        }

        public record Frame(int index, int time) {
            public int clampedTime(Animation animation) {
                return this.time > 0 ? this.time : Math.max(animation.frametime, MIN_TIME);
            }
        }
    }
}
