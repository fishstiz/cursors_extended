package io.github.fishstiz.cursors_extended.config;

public interface SharedCursorSettings<T extends SharedCursorSettings<T>> {
    boolean enabled();

    float scale();

    int xhot();

    int yhot();

    Boolean animated();

    T copy();
}
