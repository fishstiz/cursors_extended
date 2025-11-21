package io.github.fishstiz.cursors_extended.cursor;

import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import org.jspecify.annotations.Nullable;

public interface TexturedCursorType {
    default void cursors_extended$setCustom(boolean custom) {
    }

    default boolean cursors_extended$isCustom() {
        return false;
    }

    default @Nullable CursorTexture cursors_extended$getTexture() {
        return null;
    }

    default void cursors_extended$setTexture(CursorTexture texture) {
    }
}
