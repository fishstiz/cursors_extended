package io.github.fishstiz.cursors_extended.platform.services;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;

import java.nio.file.Path;

public interface PlatformHelper {
    boolean isDevelopmentEnvironment();

    Path getConfigDir();

    default String mapClassName(String namespace, String className) {
        return className;
    }

    default String unmapClassName(String namespace, String className) {
        return className;
    }

    GuiRenderState getGuiRenderState(GuiGraphics guiGraphics);
}
