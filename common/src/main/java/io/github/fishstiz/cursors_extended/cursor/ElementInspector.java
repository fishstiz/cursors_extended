package io.github.fishstiz.cursors_extended.cursor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public interface ElementInspector {
    ElementInspector NO_OP = new ElementInspector() {
    };

    default void destroy() {
    }

    default void setInspected(GuiEventListener processed, double mouseX, double mouseY) {
    }

    default void render(Minecraft minecraft, @NotNull Screen screen, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }

    default boolean isInspecting() {
        return false;
    }
}
