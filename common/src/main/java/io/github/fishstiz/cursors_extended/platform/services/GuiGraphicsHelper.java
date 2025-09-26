package io.github.fishstiz.cursors_extended.platform.services;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiElementRenderState;

public interface GuiGraphicsHelper {
    void submitGuiElementRenderState(GuiGraphics guiGraphics, GuiElementRenderState guiElementRenderState);
}
