package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.resource.CursorResourceReloader;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static io.github.fishstiz.cursors_extended.util.SettingsUtil.*;

public class Cursor {
    private static final String IMG_TYPE = ".png";
    private final @Nullable Consumer<Cursor> onLoad;
    private final CursorType type;
    private final ResourceLocation location;
    private CursorMetadata metadata = new CursorMetadata();
    private Component text;
    private String base64Image;
    private double scale;
    private int xhot;
    private int yhot;
    private boolean enabled;
    private boolean loaded;
    private int textureWidth;
    private int textureHeight;
    private long id = MemoryUtil.NULL;

    Cursor(CursorType type, @Nullable Consumer<Cursor> onLoad) {
        this.type = type;
        this.onLoad = onLoad;
        this.location = CursorResourceReloader.getDirectory().withSuffix("/" + type.toString() + IMG_TYPE);
    }

    Cursor(Cursor cursor) {
        this(cursor.type, cursor.onLoad);
    }

    void loadImage(@NotNull NativeImage image, Config.CursorSettings settings, CursorMetadata metadata) throws IOException {
        try {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            assertImageSize(imageWidth, imageHeight);

            this.base64Image = NativeImageUtil.toBase64String(image);
            this.enabled = settings.isEnabled();
            this.textureWidth = imageWidth;
            this.textureHeight = imageHeight;
            this.metadata = metadata;

            create(image, settings.getScale(), settings.getXHot(), settings.getYHot());
        } catch (Exception e) {
            this.destroy();
            throw e;
        }
    }

    protected void updateImage(double scale, int xhot, int yhot) {
        if (!this.isLoaded()) {
            return;
        }

        try (NativeImage image = NativeImageUtil.fromBase64String(base64Image)) {
            create(image, scale, xhot, yhot);
        } catch (IOException e) {
            CursorsExtended.LOGGER.error("Error updating image of {}: {}", type, e);
        }
    }

    private void create(NativeImage image, double scale, int xhot, int yhot) {
        scale = sanitizeScale(scale);
        xhot = sanitizeHotspot(xhot, image.getWidth());
        yhot = sanitizeHotspot(yhot, image.getHeight());

        long previousId = this.id;
        ByteBuffer pixels = null;

        double autoScaled = getAutoScale(scale);
        NativeImage scaledImage = null;
        try {
            if (scale != 1) {
                scaledImage = NativeImageUtil.scaleImage(image, autoScaled);
            }

            NativeImage validImage = scaledImage != null ? scaledImage : image;
            int scaledXHot = scale == 1 ? xhot : (int) Math.round(xhot * autoScaled);
            int scaledYHot = scale == 1 ? yhot : (int) Math.round(yhot * autoScaled);
            int scaledWidth = validImage.getWidth();
            int scaledHeight = validImage.getHeight();

            GLFWImage glfwImage = GLFWImage.create();
            pixels = MemoryUtil.memAlloc(scaledWidth * scaledHeight * 4);
            NativeImageUtil.writePixelsRGBA(validImage, pixels);
            glfwImage.set(scaledWidth, scaledHeight, pixels);

            this.id = GLFW.glfwCreateCursor(glfwImage, scaledXHot, scaledYHot);
            if (this.id == MemoryUtil.NULL) {
                CursorsExtended.LOGGER.error("[cursors-extended] Error creating cursor '{}'. ", this.type);
                return;
            }

            loaded = true;
            this.scale = scale;
            this.xhot = xhot;
            this.yhot = yhot;

            if (this.onLoad != null) {
                this.onLoad.accept(this);
            }
        } finally {
            if (scaledImage != null) {
                scaledImage.close();
            }
            if (pixels != null) {
                MemoryUtil.memFree(pixels);
            }
            if (previousId != MemoryUtil.NULL && this.id != previousId) {
                GLFW.glfwDestroyCursor(previousId);
            }
        }
    }

    public void destroy() {
        if (this.id != MemoryUtil.NULL) {
            GLFW.glfwDestroyCursor(this.id);
            this.id = MemoryUtil.NULL;
        }
    }

    public void reload() {
        if (this.isLoaded()) {
            this.updateImage(this.getScale(), this.getXHot(), this.getYHot());
        }
    }

    public void apply(Config.CursorSettings settings) {
        this.enable(settings.isEnabled());
        this.updateImage(settings.getScale(), settings.getXHot(), settings.getYHot());
    }

    public void enable(boolean enabled) {
        boolean previous = this.enabled;
        this.enabled = enabled;

        if (previous != this.enabled && this.isLoaded() && this.onLoad != null) {
            this.onLoad.accept(this);
        }
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public long getId() {
        return enabled ? id : MemoryUtil.NULL;
    }

    public @NotNull CursorType getType() {
        return type;
    }

    public @NotNull String getName() {
        return type.toString();
    }

    public @NotNull Component getText() {
        if (this.text == null) {
            this.text = Component.translatable("cursors_extended.options.cursor-type." + type.toString());
        }
        return this.text;
    }

    public CursorMetadata getMetadata() {
        return this.metadata;
    }

    public double getScale() {
        return this.scale;
    }

    public void setScale(double scale) {
        updateImage(scale, this.xhot, this.yhot);
    }

    public int getXHot() {
        return this.xhot;
    }

    public void setXHot(double xhot) {
        setXHot((int) xhot);
    }

    public void setXHot(int xhot) {
        updateImage(this.scale, xhot, this.yhot);
    }

    public int getYHot() {
        return this.yhot;
    }

    public void setYHot(double yhot) {
        setYHot((int) yhot);
    }

    public void setYHot(int yhot) {
        updateImage(this.scale, this.xhot, yhot);
    }

    public boolean isEnabled() {
        return enabled && isLoaded();
    }

    public boolean isLoaded() {
        return this.loaded && this.id != MemoryUtil.NULL;
    }

    public int getSpriteWidth() {
        return this.getTextureWidth();
    }

    public int getSpriteHeight() {
        return this.getTextureHeight();
    }

    public int getSpriteIndex() {
        return 0;
    }

    public int getTextureWidth() {
        assertLoaded();
        return textureWidth;
    }

    public int getTextureHeight() {
        assertLoaded();
        return textureHeight;
    }

    protected void assertLoaded() {
        if (!this.isLoaded()) {
            throw new IllegalStateException("Cursor has not been loaded");
        }
    }

    static Cursor loadOrCreateDummy(CursorType type, Consumer<Cursor> onLoad) {
        Cursor cursor = new Cursor(type, onLoad);
        CursorResourceReloader.loadCursorTexture(cursor);
        return cursor;
    }

    static Cursor createDummy(CursorType type) {
        return new Cursor(type, null);
    }

    static Cursor createDummy() {
        return createDummy(CursorType.DEFAULT);
    }
}
