package io.github.fishstiz.cursors_extended.platform.services;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

public interface GuiGraphicsHelper {
    void submitGuiElementRenderState(GuiGraphicsExtractor guiGraphics, GuiElementRenderState guiElementRenderState);
}
