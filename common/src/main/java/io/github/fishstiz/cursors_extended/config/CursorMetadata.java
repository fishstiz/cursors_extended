package io.github.fishstiz.cursors_extended.config;

import com.google.gson.*;
import io.github.fishstiz.cursors_extended.cursor.AnimationMode;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused", "FieldMayBeFinal"})
public final class CursorMetadata implements Serializable {
    public static final String FILE_TYPE = ".json";
    private Config.CursorSettings cursor = new Config.CursorSettings();
    private Animation animation;

    public Config.CursorSettings getCursorSettings() {
        return this.cursor;
    }

    public @Nullable CursorMetadata.Animation getAnimation() {
        return this.animation;
    }

    public static class Animation implements Serializable {
        private static final int MIN_TIME = 1;
        public final AnimationMode mode;
        private final int frametime;
        private final List<Frame> frames = Collections.emptyList();
        private Integer width;
        private Integer height;

        public Animation() {
            this.mode = AnimationMode.LOOP;
            this.frametime = MIN_TIME;
        }

        public int getFrametime() {
            return Math.max(this.frametime, MIN_TIME);
        }

        public List<Frame> getFrames() {
            return List.copyOf(this.frames);
        }

        public @Nullable Integer getWidth() {
            return width;
        }

        public @Nullable Integer getHeight() {
            return height;
        }

        public static class Frame implements Serializable {
            private final int index;
            private int time;

            public Frame(int index, int time) {
                this.index = index;
                this.time = time;
            }

            public int getIndex() {
                return this.index;
            }

            public int getTime(Animation animation) {
                this.time = this.time > 0 ? this.time : Math.max(animation.getFrametime(), MIN_TIME);
                return this.time;
            }

            public static class Deserializer implements JsonDeserializer<Animation.Frame> {
                @Override
                public Animation.Frame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                        return new Animation.Frame(json.getAsInt(), 0);
                    } else if (json.isJsonObject()) {
                        JsonObject obj = json.getAsJsonObject();
                        int index = obj.has("index") ? obj.get("index").getAsInt() : 0;
                        int time = obj.has("time") ? obj.get("time").getAsInt() : 0;
                        return new Animation.Frame(index, time);
                    }
                    throw new JsonParseException("Invalid Frame format");
                }
            }
        }
    }

}
