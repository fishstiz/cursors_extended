package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.GuiGraphicsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiElementRenderState;

class NeoForgeGuiGraphicsHelper implements GuiGraphicsHelper {
    static final GuiGraphicsHelper INSTANCE = new NeoForgeGuiGraphicsHelper();

    @Override
    public void submitGuiElementRenderState(GuiGraphics guiGraphics, GuiElementRenderState guiElementRenderState) {
        guiGraphics.submitGuiElementRenderState(guiElementRenderState);
    }
}