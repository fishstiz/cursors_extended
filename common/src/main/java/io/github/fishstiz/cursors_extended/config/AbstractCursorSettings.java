package io.github.fishstiz.cursors_extended.config;

import io.github.fishstiz.cursors_extended.util.SettingsUtil;

public abstract class AbstractCursorSettings<T extends AbstractCursorSettings<T>> {
    protected double scale = SettingsUtil.SCALE;
    protected int xhot = SettingsUtil.X_HOT;
    protected int yhot = SettingsUtil.Y_HOT;

    protected AbstractCursorSettings() {
    }

    public double getScale() {
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
