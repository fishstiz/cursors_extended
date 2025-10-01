package io.github.fishstiz.cursors_extended.cursor.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface InspectorDebugRenderer {
    InspectorDebugRenderer NOP = new InspectorDebugRenderer() {
    };

    static InspectorDebugRenderer create() {
        return new InspectorDebugRendererImpl();
    }

    default void destroy() {
    }

    default void onInspect(GuiEventListener inspected, double mouseX, double mouseY) {
    }

    default void render(Minecraft minecraft, Supplier<@Nullable Screen> visibleScreen, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }

    default boolean isActive() {
        return false;
    }
}
