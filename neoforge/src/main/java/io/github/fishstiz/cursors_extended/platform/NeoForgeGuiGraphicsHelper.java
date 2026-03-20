package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.GuiGraphicsHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

class NeoForgeGuiGraphicsHelper implements GuiGraphicsHelper {
    static final GuiGraphicsHelper INSTANCE = new NeoForgeGuiGraphicsHelper();

    @Override
    public void submitGuiElementRenderState(GuiGraphicsExtractor guiGraphics, GuiElementRenderState guiElementRenderState) {
        guiGraphics.submitGuiElementRenderState(guiElementRenderState);
    }
}