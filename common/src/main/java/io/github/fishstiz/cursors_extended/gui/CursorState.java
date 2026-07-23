package io.github.fishstiz.cursors_extended.gui;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorProperties;
import io.github.fishstiz.cursors_extended.cursor.Cursor;

public record CursorState(
        boolean enabled,
        float scale,
        Hotspots hotspots,
        Boolean animated
) implements CursorProperties {
    public record Hotspots(int x, int y) {
        public Hotspots x(int x) {
            return new Hotspots(x, y);
        }

        public Hotspots y(int y) {
            return new Hotspots(x, y);
        }
    }

    public CursorState(Cursor cursor) {
        Config.CursorSettings settings = CursorsExtended.CONFIG.getOrCreateSettings(cursor);
        this(settings.enabled() && cursor.isTextureEnabled(), settings.scale(), new Hotspots(settings.xhot(), settings.yhot()), settings.animated());
    }

    public CursorState(CursorProperties properties) {
        this(properties.enabled(), properties.scale(), new Hotspots(properties.xhot(), properties.yhot()), properties.animated());
    }

    @Override
    public int xhot() {
        return hotspots.x();
    }

    @Override
    public int yhot() {
        return hotspots.y();
    }

    public boolean animationEnabled() {
        return animated == null || this.animated;
    }

    public CursorState enabled(boolean enabled) {
        return new CursorState(enabled, scale, hotspots, animated);
    }

    public CursorState scale(float scale) {
        return new CursorState(enabled, scale, hotspots, animated);
    }

    public CursorState xhot(int xhot) {
        return new CursorState(enabled, scale, hotspots.x(xhot), animated);
    }

    public CursorState yhot(int yhot) {
        return new CursorState(enabled, scale, hotspots.y(yhot), animated);
    }

    public CursorState hotspots(int xhot, int yhot) {
        return new CursorState(enabled, scale, new Hotspots(xhot, yhot), animated);
    }

    public CursorState animated(Boolean animated) {
        return new CursorState(enabled, scale, hotspots, animated);
    }
}
