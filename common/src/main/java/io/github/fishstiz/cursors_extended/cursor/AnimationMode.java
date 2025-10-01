package io.github.fishstiz.cursors_extended.cursor;

import io.github.fishstiz.cursors_extended.CursorsExtended;

public enum AnimationMode {
    LOOP,
    LOOP_REVERSE,
    FORWARDS,
    REVERSE,
    OSCILLATE,
    RANDOM,
    RANDOM_CYCLE;

    public boolean isReversed() {
        return this == LOOP_REVERSE || this == REVERSE;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static AnimationMode getOrDefault(String name) {
        try {
            return AnimationMode.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            CursorsExtended.LOGGER.warn("[cursors-extended] Animation mode: '{}' does not exist. Using default 'loop'.", name);
            return AnimationMode.LOOP;
        }
    }
}
