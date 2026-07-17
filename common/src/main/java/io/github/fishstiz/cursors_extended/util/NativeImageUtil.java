package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.mixin.util.NativeImageAccess;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import static org.lwjgl.sdl.SDLError.SDL_GetError;

public class NativeImageUtil {
    private NativeImageUtil() {
    }

    public static NativeImage cropImage(NativeImage src, int xOffset, int yOffset, int width, int height) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        if (xOffset < 0 || yOffset < 0 || xOffset + width > srcWidth || yOffset + height > srcHeight) {
            CursorsExtended.LOGGER.error(
                    "[cursors-extended] Image size {}x{} is invalid. Valid size: {}x{} at y {}",
                    srcWidth, srcHeight, width, height, yOffset
            );
            return src;
        }

        NativeImage out = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = src.getPixel(x + xOffset, y + yOffset);
                out.setPixel(x, y, color);
            }
        }

        return out;
    }

    public static NativeImage scaleImage(NativeImage src, double scale) {
        int width = (int) Math.round(src.getWidth() * scale);
        int height = (int) Math.round(src.getHeight() * scale);
        NativeImage scaled = new NativeImage(width, height, true);

        for (int y = 0; y < height; y++) { // nearest neighbor
            for (int x = 0; x < width; x++) {
                int srcX = (int) (x / scale);
                int srcY = (int) (y / scale);
                scaled.setPixel(x, y, src.getPixel(srcX, srcY));
            }
        }
        return scaled;
    }

    public static void writePixelsRGBA(NativeImage image, ByteBuffer buffer) {
        int[] pixelsABGR = image.getPixelsABGR();

        for (int abgr : pixelsABGR) {
            buffer.put((byte) (abgr & 0xFF));
            buffer.put((byte) ((abgr >> 8) & 0xFF));
            buffer.put((byte) ((abgr >> 16) & 0xFF));
            buffer.put((byte) ((abgr >> 24) & 0xFF));
        }

        buffer.flip();
    }

    public static byte[] getBytes(NativeImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(baos);

        boolean success = ((NativeImageAccess) (Object) image).cursors_extended$writeToChannel(channel);
        channel.close();

        if (!success) throw new IOException("Failed to write NativeImage to PNG bytes.");

        return baos.toByteArray();
    }

    public static long createCursor(NativeImage image, CursorProperties settings) throws IOException {
        float scale = SettingsUtil.sanitizeScale(settings.scale());
        int xhot = SettingsUtil.sanitizeHotspot(settings.xhot(), image.getWidth());
        int yhot = SettingsUtil.sanitizeHotspot(settings.yhot(), image.getHeight());

        float trueScale = SettingsUtil.getAutoScale(scale);
        int scaledXHot = scale == 1 ? xhot : Math.round(xhot * trueScale);
        int scaledYHot = scale == 1 ? yhot : Math.round(yhot * trueScale);

        ByteBuffer pixels = null;
        NativeImage scaledImage = null;
        SDL_Surface surface = null;

        try {
            if (scale != 1) {
                scaledImage = NativeImageUtil.scaleImage(image, trueScale);
            }

            NativeImage validImage = scaledImage != null ? scaledImage : image;

            pixels = MemoryUtil.memAlloc(validImage.getWidth() * validImage.getHeight() * 4);
            NativeImageUtil.writePixelsRGBA(validImage, pixels);

            surface = SDLSurface.SDL_CreateSurfaceFrom(
                    validImage.getWidth(),
                    validImage.getHeight(),
                    SDLPixels.SDL_PIXELFORMAT_RGBA32,
                    pixels,
                    validImage.getWidth() * 4
            );
            if (surface == null) {
                throw new IOException("Could not create SDL Surface for cursor: " + SDL_GetError());
            }

            long handle = SDLMouse.SDL_CreateColorCursor(surface, scaledXHot, scaledYHot);
            if (handle == MemoryUtil.NULL) {
                throw new IOException("Could not create SDL Cursor: " + SDL_GetError());
            }

            return handle;
        } finally {
            if (scaledImage != null) {
                scaledImage.close();
            }
            if (surface != null) {
                SDLSurface.SDL_DestroySurface(surface);
            }
            if (pixels != null) {
                MemoryUtil.memFree(pixels);
            }
        }
    }
}
