package io.github.fishstiz.cursors_extended.config;

import io.github.fishstiz.cursors_extended.util.SettingsUtil;

public abstract class AbstractCursorSettings<T extends AbstractCursorSettings<T>> {
    protected float scale = SettingsUtil.SCALE;
    protected int xhot = SettingsUtil.X_HOT;
    protected int yhot = SettingsUtil.Y_HOT;

    protected AbstractCursorSettings() {
    }

    public float getScale() {
        return scale;
    }

    public int getXHot() {
        return xhot;
    }

    public int getYHot() {
        return yhot;
    }

    abstract T copy();
}
