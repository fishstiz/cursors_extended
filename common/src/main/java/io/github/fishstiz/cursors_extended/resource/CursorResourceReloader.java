package io.github.fishstiz.cursors_extended.resource;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.JsonLoader;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    public static void reload(ResourceManager manager) {
        LOGGER.info("[cursors_extended] Loading cursors...");
        if (checkHash(manager)) {
            loadCursorTextures(manager);
            LOGGER.info("[cursors_extended] Loading cursors finished.");
        } else {
            CursorManager.INSTANCE.getCursors().forEach(cursor -> {
                cursor.destroy();
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().getTextureManager().release(cursor.getLocation()));
            });
        }
        CONFIG.save();
    }

    static void resetCursor() {
        if (CursorManager.INSTANCE.isRegistered(CursorType.DEFAULT)) {
            CursorManager.INSTANCE.setCurrentCursor(CursorType.DEFAULT);
        }
    }

    private static boolean checkHash(ResourceManager manager) {
        return getHash(manager.getResourceStack(DIRECTORY))
                .map(hash -> {
                    if (!Objects.equals(CONFIG.getHash(), hash)) {
                        LOGGER.info("[cursors_extended] Resource pack hash has changed, updating config...");
                        CONFIG.setHash(hash);
                        CONFIG.getGlobal().setActiveAll(false);
                        CONFIG.markSettingsStale();
                    }
                    return true;
                })
                .orElseGet(() -> {
                    LOGGER.info("[cursors_extended] No resource pack detected.");
                    CONFIG.setHash("");
                    return false;
                });
    }

    private static Optional<String> getHash(List<Resource> resources) {
        if (resources.isEmpty()) return Optional.empty();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Resource resource : resources) {
            try (PackResources packs = resource.source()) {
                out.write(resource.sourcePackId().getBytes(StandardCharsets.UTF_8));
                packs.listResources(PackType.CLIENT_RESOURCES, MOD_ID, DIRECTORY.getPath(), (resourceLocation, ioSupplier) -> {
                    if (resourceLocation.getPath().endsWith(CursorMetadata.FILE_TYPE)) {
                        try (InputStream in = ioSupplier.get()) {
                            in.transferTo(out);
                        } catch (IOException ignore) {
                        }
                    }
                });
            } catch (IOException ignore) {
            }
        }

        if (out.size() == 0) {
            return Optional.empty();
        }

        HashCode hash = Hashing.murmur3_32_fixed().hashBytes(out.toByteArray());
        return Optional.of(hash.toString());
    }

    public static boolean isResourceSetting(@NotNull Cursor cursor, @Nullable Config.CursorSettings settings) {
        return SettingsUtil.equalSettings(cursor.getMetadata().getCursorSettings(), settings, true);
    }

    public static boolean retoreActiveResourceSettings(@NotNull Cursor cursor) {
        if (cursor.isLoaded()) {
            CONFIG.replaceActiveSettings(cursor.getMetadata().getCursorSettings(), cursor);
            cursor.apply(CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor)));
            return true;
        } else {
            LOGGER.error("Failed to apply resource settings for '{}'", cursor.getName());
        }
        return false;
    }

    public static void restoreResourceSettings() {
        for (Cursor cursor : CursorManager.INSTANCE.getCursors()) {
            Config.CursorSettings settings = CONFIG.getOrCreateSettings(cursor);
            settings.merge(cursor.getMetadata().getCursorSettings());
            cursor.apply(CONFIG.getGlobal().apply(settings));
        }
    }

    private static void loadCursorTextures(ResourceManager manager) {
        for (Cursor cursor : CursorManager.INSTANCE.getCursors()) {
            loadCursorTexture(manager, cursor);
        }
    }

    public static void loadCursorTexture(Cursor cursor) {
        loadCursorTexture(Minecraft.getInstance().getResourceManager(), cursor);
    }

    public static boolean loadCursorTexture(ResourceManager manager, Cursor cursor) {
        ResourceLocation location = cursor.getLocation();
        Optional<Resource> cursorResource = manager.getResource(location);

        try {
            if (cursorResource.isEmpty()) {
                LOGGER.error("[cursors-extended] Cursor Type: '{}' not found", cursor.getName());
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
                LOGGER.error("[cursors-extended] Failed to load cursor at '{}': {}", location, e.getMessage());
                return false;
            }
        } finally {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().getTextureManager().release(location));
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
