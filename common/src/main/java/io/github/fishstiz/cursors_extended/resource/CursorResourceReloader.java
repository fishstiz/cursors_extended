package io.github.fishstiz.cursors_extended.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.JsonLoader;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static io.github.fishstiz.cursors_extended.CursorsExtended.*;

public class CursorResourceReloader implements PreparableReloadListener {
    private static final ResourceLocation DIRECTORY = CursorsExtended.loc("textures/gui/sprites/cursors");

    public static ResourceLocation getDirectory() {
        return DIRECTORY;
    }

    private static void resetCursor() {
        Minecraft.getInstance().getWindow().selectCursor(CursorType.DEFAULT);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(
            SharedState sharedState,
            Executor backgroundExecutor,
            PreparationBarrier preparationBarrier,
            Executor gameExecutor
    ) {
        gameExecutor.execute(CursorResourceReloader::resetCursor);
        return CompletableFuture.runAsync(() -> reload(sharedState.resourceManager()), backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenRunAsync(CursorResourceReloader::resetCursor, gameExecutor);
    }

    public static void reload() {
        reload(Minecraft.getInstance().getResourceManager());
    }

    private static void reload(ResourceManager manager) {
        Services.PLATFORM.packHashing().aggregateHash(manager, getDirectory())
                .ifPresentOrElse(hash -> {
                    LOGGER.info("[cursors_extended] Loading cursors...");
                    if (!Objects.equals(CONFIG.getHash(), hash)) {
                        LOGGER.info("[cursors_extended] Resource pack hash has changed, updating config...");
                        CONFIG.setHash(hash);
                        CONFIG.getGlobal().setActiveAll(false);
                        CONFIG.markSettingsStale();
                    }

                    loadCursorTextures(manager);
                    LOGGER.info("[cursors_extended] Loading cursors finished.");
                }, () -> {
                    LOGGER.info("[cursors_extended] No resource pack detected.");
                    CONFIG.setHash("");

                    CursorManager.INSTANCE.getCursors().forEach(cursor -> {
                        cursor.destroy();
                        releaseTexture(cursor);
                    });
                });

        CONFIG.save();
    }

    private static void releaseTexture(Cursor cursor) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getTextureManager().release(cursor.getLocation()));
    }

    private static void loadCursorTextures(ResourceManager manager) {
        for (Cursor cursor : CursorManager.INSTANCE.getCursors()) {
            loadCursorTexture(manager, cursor);
        }
    }

    public static boolean loadCursorTexture(Cursor cursor) {
        return loadCursorTexture(Minecraft.getInstance().getResourceManager(), cursor);
    }

    private static boolean loadCursorTexture(ResourceManager manager, Cursor cursor) {
        ResourceLocation location = cursor.getLocation();
        Optional<Resource> cursorResource = manager.getResource(location);

        try {
            if (cursorResource.isEmpty()) {
                LOGGER.error("[cursors_extended] Cursor Type: '{}' not found", cursor.getName());
                cursor.destroy();
                return false;
            }

            try (InputStream cursorStream = cursorResource.get().open(); NativeImage image = NativeImage.read(cursorStream)) {
                CursorMetadata metadata = loadMetadata(manager, location, cursorResource.get());
                if (CONFIG.isStale(cursor)) {
                    CONFIG.getOrCreateSettings(cursor).merge(metadata.getCursorSettings());
                }
                CursorManager.INSTANCE.loadCursor(cursor, image, CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor)), metadata);
                return true;
            } catch (IOException e) {
                LOGGER.error("[cursors_extended] Failed to load cursor at '{}': {}", location, e.getMessage());
                return false;
            }
        } finally {
            releaseTexture(cursor);
        }
    }

    private static CursorMetadata loadMetadata(ResourceManager manager, ResourceLocation location, Resource cursorResource) {
        return manager.getResourceStack(location.withSuffix(CursorMetadata.FILE_TYPE))
                .stream()
                .filter(metadata -> metadata.sourcePackId().equals(cursorResource.sourcePackId()))
                .findFirst()
                .map(metadata -> JsonLoader.fromResource(CursorMetadata.class, metadata, "Cursor Metadata at  " + location))
                .orElse(new CursorMetadata());
    }
}
