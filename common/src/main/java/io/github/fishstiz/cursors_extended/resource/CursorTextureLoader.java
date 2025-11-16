package io.github.fishstiz.cursors_extended.resource;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.JsonLoader;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.resource.texture.AnimationState;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.lifecycle.ClientStartedListener;
import io.github.fishstiz.cursors_extended.resource.texture.AnimatedCursorTexture;
import io.github.fishstiz.cursors_extended.resource.texture.BasicCursorTexture;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static io.github.fishstiz.cursors_extended.CursorsExtended.*;
import static io.github.fishstiz.cursors_extended.util.SettingsUtil.*;

public class CursorTextureLoader implements PreparableReloadListener, ClientStartedListener {
    private final Map<String, CursorMetadata> preparedMetadata = new Object2ObjectOpenHashMap<>();
    private final CursorRegistry registry;
    private Minecraft minecraft;
    private boolean prepared;

    public CursorTextureLoader(CursorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onClientStarted(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(
            SharedState sharedState,
            Executor backgroundExecutor,
            PreparationBarrier preparationBarrier,
            Executor gameExecutor
    ) {
        prepared = false;

        return CompletableFuture.runAsync(() -> prepare(sharedState.resourceManager()), backgroundExecutor)
                .thenCompose(preparationBarrier::wait);
    }

    public void reload() {
        prepare(minecraft.getResourceManager());
        loadTextures(registry.getCursors());
    }

    private Optional<String> prepareMetadataHash(ResourceManager manager, Iterable<Cursor> hashableCursors) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (Cursor cursor : hashableCursors) {
            ResourceLocation path = getExpectedPath(cursor.cursorType());
            manager.getResource(path.withSuffix(CursorMetadata.FILE_TYPE)).ifPresentOrElse(resource -> {
                try {
                    // this is bugged, the resource should be the image, not the metadata, so that the image source and metadata source are the same.
                    // fixing this will reset configs
                    CursorMetadata metadata = loadMetadata(manager, path, resource.sourcePackId());
                    preparedMetadata.put(cursor.name(), metadata);

                    out.write(resource.sourcePackId().getBytes(StandardCharsets.UTF_8));
                    writeBytes(out, metadata);
                } catch (Exception ignore) {
                }
            }, () -> preparedMetadata.put(cursor.name(), new CursorMetadata()));
        }

        return out.size() == 0
                ? Optional.empty()
                : Optional.of(Hashing.murmur3_32_fixed().hashBytes(out.toByteArray()).toString());
    }


    private void prepare(ResourceManager manager) {
        preparedMetadata.clear();

        boolean dirty = prepareMetadataHash(manager, registry.getInternalCursors()).map(hash -> {
            boolean changed = !Objects.equals(CONFIG.getHash(), hash);
            if (changed) {
                LOGGER.info("[cursors_extended] Resource pack hash has changed, updating config...");
                CONFIG.setHash(hash);
                CONFIG.getGlobal().setActiveAll(false);
                CONFIG.markSettingsStale();
            }
            return changed;
        }).orElseGet(() -> {
            boolean changed = CONFIG.getHash() != null && !CONFIG.getHash().isEmpty();
            LOGGER.info("[cursors_extended] No resource pack detected.");
            CONFIG.setHash("");
            return changed;
        });

        for (Cursor cursor : registry.getCursors()) {
            if (cursor.isCustom()) continue;

            cursor.setLazy(true);

            CursorMetadata metadata = preparedMetadata.computeIfAbsent(cursor.name(), type -> {
                ResourceLocation path = getExpectedPath(cursor.cursorType());
                return manager.getResource(path.withSuffix(CursorMetadata.FILE_TYPE))
                        .map(resource -> loadMetadata(manager, path, resource.sourcePackId()))
                        .orElse(new CursorMetadata());
            });

            if (CONFIG.isStale(cursor)) {
                CONFIG.getOrCreateSettings(cursor).mergeSelective(metadata.cursor());
                dirty = true;
            }
        }

        prepared = true;
        if (dirty) CONFIG.save();
    }

    public void releaseTexture(Cursor cursor) {
        CursorTexture texture = cursor.getTexture();

        if (texture != null) {
            texture.close();
            cursor.setTexture(null);
            minecraft.execute(() -> minecraft.getTextureManager().release(texture.texturePath()));
        }
    }

    private boolean loadTexture(ResourceManager manager, Cursor cursor) {
        if (!prepared) return false;

        CursorTexture previousTexture = cursor.getTexture();
        ResourceLocation path = getExpectedPath(cursor.cursorType());
        boolean loaded = false;

        try {
            var resource = manager.getResource(path).orElse(null);
            if (resource != null) {
                try (InputStream in = resource.open(); NativeImage image = NativeImage.read(in)) {
                    assertImageSize(image.getWidth(), image.getHeight());

                    CursorMetadata metadata = preparedMetadata.getOrDefault(cursor.name(), loadMetadata(manager, path, resource.sourcePackId()));
                    CursorProperties settings = CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor));
                    CursorTexture texture = metadata.animation() != null
                            ? new AnimatedCursorTexture(AnimationState.of(metadata.animation().mode()), image, path, metadata, settings)
                            : new BasicCursorTexture(image, path, metadata, settings);

                    cursor.setTexture(texture);
                    minecraft.execute(() -> minecraft.getTextureManager().release(path));
                    loaded = true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("[cursors_extended] Failed to load cursor texture for '{}'. ", cursor.cursorType(), e);
        } finally {
            if (previousTexture != null) {
                previousTexture.close();
            }
        }

        if (!loaded) releaseTexture(cursor);
        cursor.setLazy(false);
        return loaded;
    }

    public boolean loadTexture(Cursor cursor) {
        return loadTexture(minecraft.getResourceManager(), cursor);
    }

    private void loadTextures(ResourceManager manager, Collection<Cursor> cursors) {
        for (Cursor cursor : cursors) {
            loadTexture(manager, cursor);
        }
    }

    public void lazyLoadTexture(Cursor cursor) {
        if (cursor.isCustom()) return;

        cursor = CONFIG.getOrCreateSettings(cursor).enabled() ? cursor : registry.get(CursorType.DEFAULT);
        if (CONFIG.getOrCreateSettings(cursor).enabled() && cursor.isLazy()) {
            loadTexture(cursor);
        }
    }

    public void loadTextures(Collection<Cursor> cursors) {
        loadTextures(minecraft.getResourceManager(), cursors);
    }

    public void updateTexture(Cursor cursor, float scale, int xhot, int yhot) {
        CursorTexture texture = cursor.getTexture();
        if (texture == null) return;

        Config.CursorSettings settings = CONFIG.getOrCreateSettings(cursor).copy();
        settings.setScale(scale);
        settings.setXHot(cursor, xhot);
        settings.setYHot(cursor, yhot);

        try {
            CursorTexture updatedTexture = texture.recreate(settings);
            cursor.setTexture(updatedTexture);
            texture.close();
        } catch (Exception e) {
            LOGGER.error("[cursors_extended] Failed to update texture of cursor '{}'.", cursor.cursorType(), e);
            cursor.setTexture(texture);
        }
    }

    public void updateTexture(Cursor cursor, CursorProperties settings) {
        updateTexture(cursor, settings.scale(), settings.xhot(), settings.yhot());
    }

    private CursorMetadata loadMetadata(ResourceManager manager, ResourceLocation location, String source) {
        return manager.getResourceStack(location.withSuffix(CursorMetadata.FILE_TYPE))
                .stream()
                .filter(metadata -> metadata.sourcePackId().equals(source))
                .findFirst()
                .map(metadata -> JsonLoader.fromResource(CursorMetadata.class, metadata))
                .orElse(new CursorMetadata());
    }

    private static void writeBytes(ByteArrayOutputStream out, CursorMetadata metadata) throws IOException {
        CursorMetadata.CursorSettings cs = metadata.cursor();
        out.write(Float.toString(cs.scale()).getBytes(StandardCharsets.UTF_8));
        out.write(Integer.toString(cs.xhot()).getBytes(StandardCharsets.UTF_8));
        out.write(Integer.toString(cs.yhot()).getBytes(StandardCharsets.UTF_8));
        out.write(Boolean.toString(cs.enabled()).getBytes(StandardCharsets.UTF_8));
        if (cs.animated() != null) {
            out.write(Boolean.toString(cs.animated()).getBytes(StandardCharsets.UTF_8));
        }

        CursorMetadata.Animation anim = metadata.animation();
        if (anim != null) {
            out.write(anim.mode().name().getBytes(StandardCharsets.UTF_8));
            out.write(Integer.toString(anim.frametime()).getBytes(StandardCharsets.UTF_8));
            if (anim.width() != null) {
                out.write(Integer.toString(anim.width()).getBytes(StandardCharsets.UTF_8));
            }
            if (anim.height() != null) {
                out.write(Integer.toString(anim.height()).getBytes(StandardCharsets.UTF_8));
            }
            for (CursorMetadata.Animation.Frame f : anim.frames()) {
                out.write(Integer.toString(f.index()).getBytes(StandardCharsets.UTF_8));
                out.write(Integer.toString(f.clampedTime(anim)).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static ResourceLocation getExpectedPath(CursorType cursorType) {
        return getDir().withSuffix("/" + cursorType.toString() + ".png");
    }

    public static ResourceLocation getDir() {
        return CursorsExtended.loc("textures/gui/sprites/cursors");
    }
}
