package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.GuiGraphicsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiElementRenderState;

class FabricGuiGraphicsHelper implements GuiGraphicsHelper {
    static final GuiGraphicsHelper INSTANCE = new FabricGuiGraphicsHelper();

    @Override
    public void submitGuiElementRenderState(GuiGraphics guiGraphics, GuiElementRenderState guiElementRenderState) {
        guiGraphics.guiRenderState.submitGuiElement(guiElementRenderState);
    }
}