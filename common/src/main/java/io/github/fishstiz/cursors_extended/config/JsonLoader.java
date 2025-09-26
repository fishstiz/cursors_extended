package io.github.fishstiz.cursors_extended.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.AnimationMode;
import net.minecraft.server.packs.resources.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class JsonLoader {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CursorMetadata.Animation.Frame.class, new CursorMetadata.Animation.Frame.Deserializer())
            .registerTypeAdapter(AnimationMode.class, new AnimationMode.Deserializer())
            .setPrettyPrinting()
            .create();

    private JsonLoader() {
    }

    public static <T extends Serializable> T fromResource(Class<T> clazz, Resource resource, String failMessage) {
        try (InputStream stream = resource.open()) {
            return fromStream(clazz, stream, failMessage);
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("[cursors-extended] Failed to open resource: {}", failMessage);
            return null;
        }
    }

    public static <T extends Serializable> T fromStream(Class<T> clazz, InputStream stream, String failMessage) {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            return GSON.fromJson(reader, clazz);
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("[cursors-extended] Failed to load file: {}", failMessage);
            return null;
        }
    }

    public static <T extends Serializable> T loadOrDefault(Class<T> clazz, Path path, Supplier<T> defaultSupplier) {
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

    public static void save(Path path, Serializable obj) {
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
}
