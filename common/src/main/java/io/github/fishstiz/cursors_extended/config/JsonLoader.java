package io.github.fishstiz.cursors_extended.config;

import com.google.gson.*;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.resource.texture.AnimationMode;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.server.packs.resources.Resource;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class JsonLoader {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CursorMetadata.Animation.Frame.class, new MetaAnimationFrameDeserializer())
            .registerTypeAdapter(CursorMetadata.CursorSettings.class, new MetaCursorSettingsDeserializer())
            .registerTypeAdapter(AnimationMode.class, new AnimationModeDeserializer())
            .setPrettyPrinting()
            .create();

    private JsonLoader() {
    }

    public static <T> T fromResource(Class<T> clazz, Resource resource) {
        try (InputStream stream = resource.open()) {
            return fromStream(clazz, stream);
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("[cursors-extended] Failed to open resource: {}", resource);
            return null;
        }
    }

    public static <T> T fromStream(Class<T> clazz, InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            return GSON.fromJson(reader, clazz);
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("[cursors-extended] Failed to load file: {}", clazz);
            return null;
        }
    }

    public static <T> T loadOrDefault(Class<T> clazz, Path path, Supplier<T> defaultSupplier) {
        path = ensureJsonSuffix(path);

        try (Reader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, clazz);
        } catch (NoSuchFileException e) {
            T obj = defaultSupplier.get();
            CursorsExtended.LOGGER.info("[cursors-extended] Creating file at '{}'...", path);
            save(path, obj);
            return obj;
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("[cursors-extended] Failed to load file at '{}'", path);
            return defaultSupplier.get();
        }
    }

    public static void save(Path path, Object obj) {
        path = ensureJsonSuffix(path);

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(obj, writer);
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("[cursors-extended] Failed to save file at '{}'", path);
        }
    }

    private static Path ensureJsonSuffix(Path path) {
        String fileName = path.getFileName().toString();
        if (!fileName.endsWith(".json")) {
            path = path.resolveSibling(fileName + ".json");
        }
        return path;
    }

    private static class MetaAnimationFrameDeserializer implements JsonDeserializer<CursorMetadata.Animation.Frame> {
        @Override
        public CursorMetadata.Animation.Frame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                return new CursorMetadata.Animation.Frame(json.getAsInt(), 0);
            } else if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                int index = obj.has("index") ? obj.get("index").getAsInt() : 0;
                int time = obj.has("time") ? obj.get("time").getAsInt() : 0;
                return new CursorMetadata.Animation.Frame(index, time);
            }
            throw new JsonParseException("Invalid Frame format");
        }
    }

    private static class MetaCursorSettingsDeserializer implements JsonDeserializer<CursorMetadata.CursorSettings> {
        @Override
        public CursorMetadata.CursorSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) throw new JsonParseException("Expected JsonObject");

            JsonObject obj = json.getAsJsonObject();

            boolean enabled = obj.has("enabled") ? obj.get("enabled").getAsBoolean() : SettingsUtil.ENABLED;
            float scale = obj.has("scale") ? obj.get("scale").getAsFloat() : SettingsUtil.SCALE;
            int xhot = obj.has("xhot") ? obj.get("xhot").getAsInt() : SettingsUtil.X_HOT;
            int yhot = obj.has("yhot") ? obj.get("yhot").getAsInt() : SettingsUtil.Y_HOT;
            Boolean animated = obj.has("animated") ? Boolean.valueOf(obj.get("animated").getAsBoolean()) : SettingsUtil.ANIMATED;

            return new CursorMetadata.CursorSettings(enabled, scale, xhot, yhot, animated);
        }
    }

    private static class AnimationModeDeserializer implements JsonDeserializer<AnimationMode> {
        @Override
        public AnimationMode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return AnimationMode.getOrDefault(json.getAsString());
        }
    }
}
