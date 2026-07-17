package io.github.fishstiz.cursors_extended.resource.texture;

public interface CursorSprite {
    int textureWidth();

    int textureHeight();

    default int spriteWidth() {
        return textureWidth();
    }

    default int spriteHeight() {
        return textureHeight();
    }

    default int spriteVOffset() {
        return 0;
    }
}
