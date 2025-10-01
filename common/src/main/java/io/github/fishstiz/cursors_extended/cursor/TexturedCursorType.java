package io.github.fishstiz.cursors_extended.cursor;

import io.github.fishstiz.cursors_extended.resource.CursorTexture;
import org.jetbrains.annotations.Nullable;

public interface TexturedCursorType {
    default @Nullable CursorTexture cursors_extended$getTexture() {
        return null;
    }

    default void cursors_extended$setTexture(CursorTexture texture) {
    }
}
