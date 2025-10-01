package io.github.fishstiz.cursors_extended.resource;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.config.JsonLoader;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.lifecycle.ClientStartedListener;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static io.github.fishstiz.cursors_extended.CursorsExtended.*;
import static io.github.fishstiz.cursors_extended.util.SettingsUtil.*;

public class CursorTextureLoader implements PreparableReloadListener, ClientStartedListener {
    private static final int RGBA_BYTES_PER_PIXEL = 4;
    private static final ResourceLocation DIRECTORY = CursorsExtended.loc("textures/gui/sprites/cursors");
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
            manager.getResource(path.withSuffix(CursorMetadata.FILE_TYPE)).ifPresent(resource -> {
                try {
                    CursorMetadata metadata = loadMetadata(manager, path, resource.sourcePackId());
                    preparedMetadata.put(cursor.name(), metadata);

                    out.write(resource.sourcePackId().getBytes(StandardCharsets.UTF_8));
                    writeBytes(out, metadata);
                } catch (Exception ignore) {
                }
            });
        }

        return out.size() == 0
                ? Optional.empty()
                : Optional.of(Hashing.murmur3_32_fixed().hashBytes(out.toByteArray()).toString());
    }


    private void prepare(ResourceManager manager) {
        preparedMetadata.clear();

        prepareMetadataHash(manager, registry.getInternalCursors()).ifPresentOrElse(hash -> {
            if (!Objects.equals(CONFIG.getHash(), hash)) {
                LOGGER.info("[cursors_extended] Resource pack hash has changed, updating config...");
                CONFIG.setHash(hash);
                CONFIG.getGlobal().setActiveAll(false);
                CONFIG.markSettingsStale();
            }
        }, () -> {
            LOGGER.info("[cursors_extended] No resource pack detected.");
            CONFIG.setHash("");
        });

        registry.getCursors().forEach(cursor -> {
            cursor.prepareReload();

            CursorMetadata metadata = preparedMetadata.computeIfAbsent(cursor.name(), type -> {
                ResourceLocation path = getExpectedPath(cursor.cursorType());
                return manager.getResource(path.withSuffix(CursorMetadata.FILE_TYPE))
                        .map(resource -> loadMetadata(manager, path, resource.sourcePackId()))
                        .orElse(new CursorMetadata());
            });

            if (CONFIG.isStale(cursor)) {
                CONFIG.getOrCreateSettings(cursor).mergeSelective(metadata.cursor());
            }
        });

        prepared = true;
        CONFIG.save();
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

        ResourceLocation path = getExpectedPath(cursor.cursorType());
        boolean loaded = manager.getResource(path)
                .map(resource -> {
                    try (InputStream in = resource.open(); NativeImage image = NativeImage.read(in)) {
                        assertImageSize(image.getWidth(), image.getHeight());

                        CursorMetadata metadata = preparedMetadata.getOrDefault(cursor.name(), loadMetadata(manager, path, resource.sourcePackId()));
                        Config.CursorSettings settings = CONFIG.getGlobal().apply(CONFIG.getOrCreateSettings(cursor));
                        float scale = sanitizeScale(settings.scale());
                        int xhot = sanitizeHotspot(settings.xhot(), image.getWidth());
                        int yhot = sanitizeHotspot(settings.yhot(), image.getHeight());

                        CursorTexture texture = metadata.animation() != null
                                ? createAnimated(image, path, settings.animated(), settings.enabled(), scale, xhot, yhot, metadata)
                                : createBasic(image, path, settings.enabled(), scale, xhot, yhot, metadata);

                        cursor.setTexture(texture);
                        minecraft.execute(() -> minecraft.getTextureManager().release(path));

                        return true;
                    } catch (Exception e) {
                        LOGGER.error("[cursors_extended] Failed to load cursor texture for '{}'. ", cursor.cursorType(), e);
                        releaseTexture(cursor);
                        return false;
                    }
                })
                .orElseGet(() -> {
                    releaseTexture(cursor);
                    return false;
                });

        cursor.reloaded();
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

    public void loadTextures(Collection<Cursor> cursors) {
        loadTextures(minecraft.getResourceManager(), cursors);
    }

    public void updateTexture(Cursor cursor, float scale, int xhot, int yhot) {
        CursorTexture texture = cursor.getTexture();

        if (texture != null) {
            NativeImage copy = null;
            try {
                copy = NativeImage.read(texture.pixels());

                CursorTexture updatedTexture = switch (texture) {
                    case BasicCursorTexture ignoreBasic ->
                            createBasic(copy, texture.texturePath(), texture.enabled(), scale, xhot, yhot, texture.metadata());
                    case AnimatedCursorTexture animated ->
                            createAnimated(copy, texture.texturePath(), animated.isAnimated(), texture.enabled(), scale, xhot, yhot, texture.metadata());
                };

                cursor.setTexture(updatedTexture);
                texture.close();
            } catch (Exception e) {
                LOGGER.error("[cursors_extended] Failed to update texture of cursor '{}'", cursor.cursorType(), e);
                cursor.setTexture(texture);
                if (copy != null) {
                    copy.close();
                }
            }
        }
    }

    public void updateTexture(Cursor cursor, Config.CursorSettings settings) {
        updateTexture(cursor, settings.scale(), settings.xhot(), settings.yhot());
    }

    private CursorMetadata loadMetadata(ResourceManager manager, ResourceLocation location, String source) {
        return manager.getResourceStack(location.withSuffix(CursorMetadata.FILE_TYPE))
                .stream()
                .filter(metadata -> metadata.sourcePackId().equals(source))
                .findFirst()
                .map(metadata -> JsonLoader.fromResource(CursorMetadata.class, metadata, "Cursor Metadata at  " + location))
                .orElse(new CursorMetadata());
    }

    private static BasicCursorTexture createBasic(
            NativeImage image,
            ResourceLocation path,
            boolean enabled,
            float scale,
            int xhot,
            int yhot,
            CursorMetadata metadata
    ) throws IOException {
        float trueScale = getAutoScale(scale);
        int scaledXHot = scale == 1 ? xhot : Math.round(xhot * trueScale);
        int scaledYHot = scale == 1 ? yhot : Math.round(yhot * trueScale);

        ByteBuffer pixels = null;
        NativeImage scaledImage = null;

        try {
            if (scale != 1) {
                scaledImage = NativeImageUtil.scaleImage(image, trueScale);
            }

            GLFWImage glfwImage = GLFWImage.create();
            NativeImage validImage = scaledImage != null ? scaledImage : image;

            pixels = MemoryUtil.memAlloc(validImage.getWidth() * validImage.getHeight() * RGBA_BYTES_PER_PIXEL);
            NativeImageUtil.writePixelsRGBA(validImage, pixels);

            glfwImage.set(validImage.getWidth(), validImage.getHeight(), pixels);

            long handle = GLFW.glfwCreateCursor(glfwImage, scaledXHot, scaledYHot);
            if (handle == MemoryUtil.NULL) {
                throw new IOException("Could not create GLFW Cursor");
            }

            return new BasicCursorTexture(enabled, handle, scale, xhot, yhot, image, path, metadata);
        } finally {
            if (scaledImage != null) {
                scaledImage.close();
            }
            if (pixels != null) {
                MemoryUtil.memFree(pixels);
            }
        }
    }

    private static AnimatedCursorTexture createAnimated(
            NativeImage image,
            ResourceLocation path,
            Boolean animated,
            boolean enabled,
            float scale,
            int xhot,
            int yhot,
            CursorMetadata metadata
    ) throws IOException {
        CursorMetadata.Animation animation = Objects.requireNonNull(metadata.animation(), "metadata animation must not be null");

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int preferredFrameSize = Math.min(imageWidth, imageHeight);

        int frameWidth = Math.min(Math.abs(getOrDefault(animation.width(), preferredFrameSize)), imageWidth);
        int frameHeight = Math.min(Math.abs(getOrDefault(animation.height(), preferredFrameSize)), imageHeight);
        assertImageSize(frameWidth, frameHeight);

        int availableFrames = image.getHeight() / frameHeight;

        List<BasicCursorTexture> textures = new ObjectArrayList<>(availableFrames);
        try {
            for (int i = 0; i < availableFrames; i++) {
                int yOffset = i * frameHeight;
                try (NativeImage croppedImage = NativeImageUtil.cropImage(image, 0, yOffset, frameWidth, frameHeight)) {
                    textures.add(createBasic(croppedImage, path, enabled, scale, xhot, yhot, metadata));
                }
            }

            AnimatedCursorTexture.Frame baseFrame = new AnimatedCursorTexture.Frame(textures.getFirst(), 0, animation.frametime());
            List<AnimatedCursorTexture.Frame> frames = createAnimationFrames(animation, textures, availableFrames);
            return new AnimatedCursorTexture(animated, enabled, scale, xhot, yhot, image, path, metadata, baseFrame, frames);
        } catch (Exception e) {
            textures.forEach(BasicCursorTexture::close);
            throw e;
        }
    }

    private static List<AnimatedCursorTexture.Frame> createAnimationFrames(
            CursorMetadata.Animation animation,
            List<BasicCursorTexture> textures,
            int availableFrames
    ) {
        List<AnimatedCursorTexture.Frame> frames = new ObjectArrayList<>();

        if (animation.frames().isEmpty()) {
            for (int i = 0; i < availableFrames; i++) {
                frames.add(new AnimatedCursorTexture.Frame(textures.get(i), i, animation.frametime()));
            }
            return frames;
        }

        for (CursorMetadata.Animation.Frame frame : animation.frames()) {
            int index = frame.index();
            if (index < 0 || index >= availableFrames) {
                LOGGER.warn("[cursors_extended] Sprite does not exist on index {}.", index);
                continue;
            }
            frames.add(new AnimatedCursorTexture.Frame(textures.get(index), index, frame.clampedTime(animation)));
        }

        if (frames.isEmpty()) {
            LOGGER.warn("[cursors_extended] No valid frames found, using first frame as fallback");
            frames.add(new AnimatedCursorTexture.Frame(textures.getFirst(), 0, animation.frametime()));
        }

        return frames;
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
        return DIRECTORY.withSuffix("/" + cursorType.toString() + ".png");
    }

    public static ResourceLocation getDir() {
        return DIRECTORY;
    }
}
