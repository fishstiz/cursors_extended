package io.github.fishstiz.cursors_extended.services;

import org.lwjgl.sdl.SDL_CursorFrameInfo;
import org.lwjgl.sdl.SDL_Surface;

import java.util.concurrent.atomic.AtomicInteger;

public final class SDLMouseOpsCompat implements SDLMouseOpsHandler {
    // AtomicInteger is used to preserve context when ixeris steps to another thread and recurses.
    // Not actually thread-safe, but thanks to SDL3 returning values on all these methods ixeris is forced to block.
    private static final AtomicInteger IS_SET_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_CREATE_STANDARD_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_CREATE_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_CREATE_ANIMATED_CURSOR = new AtomicInteger(0);

    SDLMouseOpsCompat() {
    }

    public static boolean isSettingCursor() {
        return IS_SET_CURSOR.get() > 0;
    }

    public static boolean isCreatingStandardCursor() {
        return IS_CREATE_STANDARD_CURSOR.get() > 0;
    }

    public static boolean isCreatingCursor() {
        return IS_CREATE_CURSOR.get() > 0;
    }

    public static boolean isCreatingAnimatedCursor() {
        return IS_CREATE_ANIMATED_CURSOR.get() > 0;
    }

    @Override
    public void beforeCreateStandardCursor(int shape) {
        IS_CREATE_STANDARD_CURSOR.incrementAndGet();
    }

    @Override
    public void afterCreateStandardCursor(long handle, int shape) {
        IS_CREATE_STANDARD_CURSOR.decrementAndGet();
    }

    @Override
    public void beforeCreateCursor(SDL_Surface surface, int xhot, int yhot) {
        IS_CREATE_CURSOR.incrementAndGet();
    }

    @Override
    public void afterCreateCursor(long handle, SDL_Surface surface, int xhot, int yhot) {
        IS_CREATE_CURSOR.decrementAndGet();
    }

    @Override
    public void beforeCreateAnimatedCursor(SDL_CursorFrameInfo.Buffer frames, int xhot, int yhot) {
        IS_CREATE_ANIMATED_CURSOR.incrementAndGet();
    }

    @Override
    public void afterCreateAnimatedCursor(long handle, SDL_CursorFrameInfo.Buffer frames, int xhot, int yhot) {
        IS_CREATE_ANIMATED_CURSOR.decrementAndGet();
    }

    @Override
    public void beforeSetCursor(long handle) {
        IS_SET_CURSOR.incrementAndGet();
    }

    @Override
    public void afterSetCursor(long handle) {
        IS_SET_CURSOR.decrementAndGet();
    }
}
