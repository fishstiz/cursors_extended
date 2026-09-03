package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.resource.texture.*;
import io.github.fishstiz.cursors_extended.services.SDLMouseOpsHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.SharedConstants;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spng.SPNG;
import org.lwjgl.util.spng.spng_ihdr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;

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

    // copy of NativeImage#writeToFile, except we write to baos
    public static byte[] getBytes(NativeImage image) throws IOException {
        if (image.getPointer() == 0) {
            throw new IllegalStateException("Image is not allocated.");
        }

        long context = SPNG.spng_ctx_new(SPNG.SPNG_CTX_ENCODER);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             WritableByteChannel channel = Channels.newChannel(output);
             Arena arena = Arena.ofConfined();
             MemoryStack stack = MemoryStack.stackPush()) {

            int width = image.getWidth();
            int height = Math.min(image.getHeight(), Integer.MAX_VALUE / width / image.format().components());

            NativeImage.WriteCallback writer = new NativeImage.WriteCallback(channel);
            MemorySegment writerUpcall = writer.createUpcall(arena);

            checkSpngError("set output", SPNG.nspng_set_png_stream(context, writerUpcall.address(), 0L));

            spng_ihdr header = spng_ihdr.calloc(stack)
                    .width(width)
                    .height(height)
                    .color_type((byte) image.format().pngColorType)
                    .bit_depth((byte) 8);

            checkSpngError("set header", SPNG.spng_set_ihdr(context, header));
            checkSpngError("write image", SPNG.nspng_encode_image(
                    context,
                    image.getPointer(),
                    getSize(image),
                    SPNG.SPNG_FMT_PNG,
                    SPNG.SPNG_ENCODE_FINALIZE
            ));

            writer.throwIfException();
            return output.toByteArray();
        } finally {
            SPNG.spng_ctx_free(context);
        }
    }

    private static long getSize(NativeImage image) {
        return (long) image.getWidth() * (long) image.getHeight() * (long) image.format().components();
    }

    // copy of NativeImage#checkSpngError
    private static void checkSpngError(final String operation, final int result) throws IOException {
        if (result != 0) {
            throw new IOException("SPNG operation '" + operation + "' failed: " + SPNG.spng_strerror(result) + " (" + result + ")");
        }
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
            if (scale != 1) scaledImage = NativeImageUtil.scaleImage(image, trueScale);
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
                throw new IOException("Failed to create SDL Surface for cursor: " + SDLError.SDL_GetError());
            }

            SDLMouseOpsHandler.INSTANCE.beforeCreateCursor(surface, scaledXHot, scaledYHot);
            long handle = SDLMouse.SDL_CreateColorCursor(surface, scaledXHot, scaledYHot);
            SDLMouseOpsHandler.INSTANCE.afterCreateCursor(handle, surface, scaledXHot, scaledYHot);

            if (handle == MemoryUtil.NULL) {
                throw new IOException("Failed to create SDL Cursor: " + SDLError.SDL_GetError());
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

    public static long createAnimatedCursor(
            NativeImage image,
            AnimationMode mode,
            List<AnimatedCursorFrame> frames,
            CursorProperties settings
    ) throws IOException, OSUnsupportedAnimationException {
        if (mode.random()) {
            throw new OSUnsupportedAnimationException("Random animation modes are not supported by native animated cursors.");
        }

        float scale = SettingsUtil.sanitizeScale(settings.scale());
        int xhot = SettingsUtil.sanitizeHotspot(settings.xhot(), image.getWidth());
        int yhot = SettingsUtil.sanitizeHotspot(settings.yhot(), image.getHeight());

        float trueScale = SettingsUtil.getAutoScale(scale);
        int scaledXHot = scale == 1 ? xhot : Math.round(xhot * trueScale);
        int scaledYHot = scale == 1 ? yhot : Math.round(yhot * trueScale);

        List<AnimatedCursorFrame> sortedFrames = new ObjectArrayList<>(frames.size());
        switch (mode) {
            case LOOP,
                 LOOP_REVERSE,
                 FORWARDS,
                 REVERSE -> sortedFrames.addAll(frames);
            case OSCILLATE -> {
                sortedFrames.addAll(frames);
                for (int i = frames.size() - 2; i > 0; i--) {
                    sortedFrames.add(frames.get(i));
                }
            }
            default ->
                    throw new OSUnsupportedAnimationException("Unsupported animation mode for native animated cursor " + mode);
        }
        int frameCount = sortedFrames.size();

        NativeImage scaledImage = null;
        ByteBuffer pixels = null;
        Int2ObjectMap<SDL_Surface> surfaces = new Int2ObjectOpenHashMap<>();
        SDL_CursorFrameInfo.Buffer frameBuffer = null;

        try {
            if (scale != 1) scaledImage = NativeImageUtil.scaleImage(image, trueScale);
            NativeImage validImage = scaledImage != null ? scaledImage : image;

            int pitch = validImage.getWidth() * 4;
            pixels = MemoryUtil.memAlloc(validImage.getWidth() * validImage.getHeight() * 4);
            NativeImageUtil.writePixelsRGBA(validImage, pixels);

            frameBuffer = SDL_CursorFrameInfo.malloc(frameCount);

            for (int i = 0; i < frameCount; i++) {
                AnimatedCursorFrame frame = sortedFrames.get(i);
                int duration = mode.oneShot() && i == frameCount - 1 ? 0 : frame.duration() * SharedConstants.MILLIS_PER_TICK;
                int vOffset = frame.spriteVOffset();

                SDL_Surface surface = surfaces.get(vOffset);
                if (surface == null) {
                    int scaledFrameWidth = scale == 1 ? frame.spriteWidth() : Math.round(frame.spriteWidth() * trueScale);
                    int scaledFrameHeight = scale == 1 ? frame.spriteHeight() : Math.round(frame.spriteHeight() * trueScale);

                    int yOffset = scale == 1 ? vOffset : Math.round(vOffset * trueScale);
                    long pixelPointer = MemoryUtil.memAddress(pixels) + (long) yOffset * pitch;
                    ByteBuffer frameSlice = MemoryUtil.memByteBuffer(pixelPointer, pitch * scaledFrameHeight);
                    surface = SDLSurface.SDL_CreateSurfaceFrom(
                            scaledFrameWidth,
                            scaledFrameHeight,
                            SDLPixels.SDL_PIXELFORMAT_RGBA32,
                            frameSlice,
                            pitch
                    );
                    if (surface == null) {
                        throw new IOException("Failed to create SDL Surface for animated cursor frame: " + SDLError.SDL_GetError());
                    }
                    surfaces.put(vOffset, surface);
                }

                frameBuffer.get(i).surface(surface).duration(duration);
            }

            AnimatedCursorFrame firstFrame = sortedFrames.getFirst();
            int hotFrameWidth = scale == 1 ? firstFrame.spriteWidth() : Math.round(firstFrame.spriteWidth() * trueScale);
            int hotFrameHeight = scale == 1 ? firstFrame.spriteHeight() : Math.round(firstFrame.spriteHeight() * trueScale);

            int clampedXHot = Math.min(scaledXHot, hotFrameWidth - 1);
            int clampedYHot = Math.min(scaledYHot, hotFrameHeight - 1);

            SDLMouseOpsHandler.INSTANCE.beforeCreateAnimatedCursor(frameBuffer, clampedXHot, clampedYHot);
            long handle = SDLMouse.SDL_CreateAnimatedCursor(frameBuffer, clampedXHot, clampedYHot);
            SDLMouseOpsHandler.INSTANCE.afterCreateAnimatedCursor(handle, frameBuffer, clampedXHot, clampedYHot);

            if (handle == MemoryUtil.NULL) {
                throw new IOException("Failed to create native animated cursor: " + SDLError.SDL_GetError());
            }

            return handle;
        } finally {
            if (frameBuffer != null) {
                frameBuffer.free();
            }
            for (SDL_Surface surface : surfaces.values()) {
                SDLSurface.SDL_DestroySurface(surface);
            }
            if (pixels != null) {
                MemoryUtil.memFree(pixels);
            }
            if (scaledImage != null) {
                scaledImage.close();
            }
        }
    }
}
