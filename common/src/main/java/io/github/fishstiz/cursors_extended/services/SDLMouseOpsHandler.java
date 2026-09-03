package io.github.fishstiz.cursors_extended.services;

import org.lwjgl.sdl.SDL_CursorFrameInfo;
import org.lwjgl.sdl.SDL_Surface;

public interface SDLMouseOpsHandler {
    SDLMouseOpsHandler INSTANCE = ServiceFactory.INSTANCE.createSDLMouseOpsHandler();

    default void beforeCreateStandardCursor(int shape) {
    }

    default void afterCreateStandardCursor(long handle, int shape) {
    }

    default void beforeCreateCursor(SDL_Surface surface, int xhot, int yhot) {
    }

    default void afterCreateCursor(long handle, SDL_Surface surface, int xhot, int yhot) {
    }

    default void beforeCreateAnimatedCursor(SDL_CursorFrameInfo.Buffer frames, int xhot, int yhot) {
    }

    default void afterCreateAnimatedCursor(long handle, SDL_CursorFrameInfo.Buffer frames, int xhot, int yhot) {
    }

    default void beforeSetCursor(long handle) {
    }

    default void afterSetCursor(long handle) {
    }
}
