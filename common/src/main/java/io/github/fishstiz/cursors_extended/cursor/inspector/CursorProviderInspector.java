package io.github.fishstiz.cursors_extended.cursor.inspector;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class CursorProviderInspector {
    public static final CursorProviderInspector INSTANCE = new CursorProviderInspector();
    private InspectorDebugRenderer debugRenderer = InspectorDebugRenderer.NO_OP;
    private @Nullable Screen visibleScreen;

    private CursorProviderInspector() {
    }

    public CursorType inspect() {
        Screen screen = this.getVisibleScreen();

        Minecraft minecraft = Minecraft.getInstance();
        MouseHandler mouseHandler = minecraft.mouseHandler;
        Window window = minecraft.getWindow();
        double mouseX = (int) mouseHandler.getScaledXPos(window);
        double mouseY = (int) mouseHandler.getScaledYPos(window);

        return screen != null ? this.inspect(screen, mouseX, mouseY) : CursorType.DEFAULT;
    }

    /**
     * Recursively inspects a GUI element (and its children if it’s a container)
     * to determine the appropriate cursor type at the given mouse position.
     * <p>
     * Stops at the first hovered child, matching the default implementation of
     * {@link ContainerEventHandler#mouseClicked}.
     */
    public CursorType inspect(GuiEventListener element, double mouseX, double mouseY) {
        if (CursorTypeUtil.isHovered(element, mouseX, mouseY)) {
            if (element instanceof ContainerEventHandler container) {
                for (GuiEventListener child : container.children()) {
                    CursorType cursorType = this.inspect(child, mouseX, mouseY);
                    if (CursorTypeUtil.nonDefault(cursorType)) {
                        return cursorType;
                    }
                }
            }
            if (element instanceof CursorProvider provider) {
                debugRenderer.setInspected(element, mouseX, mouseY);
                return provider.cursors_extended$cursorType(mouseX, mouseY);
            }
        }
        debugRenderer.setInspected(element, mouseX, mouseY);
        return CursorType.DEFAULT;
    }

    public void setVisibleScreen(Screen visibleScreen) {
        if (Minecraft.getInstance().screen == null) {
            this.visibleScreen = visibleScreen;
        }
    }

    public @Nullable Screen getVisibleScreen() {
        Screen screen = Minecraft.getInstance().screen;
        return screen != null ? screen : this.visibleScreen;
    }

    public void toggleDebugger() {
        debugRenderer.destroy();
        debugRenderer = this.debugRenderer == InspectorDebugRenderer.NO_OP ? new InspectorDebugRendererImpl() : InspectorDebugRenderer.NO_OP;
    }

    public void renderDebugger(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.debugRenderer.render(minecraft, screen, guiGraphics, mouseX, mouseY);
    }

    public boolean isDebugging() {
        return this.debugRenderer.isActive();
    }
}
