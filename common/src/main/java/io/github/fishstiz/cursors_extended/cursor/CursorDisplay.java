package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.inspector.InspectorDebugRenderer;
import io.github.fishstiz.cursors_extended.lifecycle.ClientStartedListener;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class CursorDisplay implements ClientStartedListener {
    private final CursorRegistry registry;
    private Minecraft minecraft;
    private InspectorDebugRenderer debugRenderer = InspectorDebugRenderer.NOP;
    private CursorRenderer cursorRenderer;
    private @Nullable Screen visibleScreen;

    public CursorDisplay(CursorRegistry registry) {
        this.registry = registry;
        this.cursorRenderer = new CursorRenderer.Native(registry);
    }

    @Override
    public void onClientStarted(Minecraft minecraft) {
        this.minecraft = minecraft;

        if (CursorsExtended.CONFIG.isVirtualMode() != isVirtual()) {
            this.toggleVirtual();
        }
    }

    public CursorType inspectGui(Window window) {
        Screen screen = getVisibleScreen();

        if (screen != null) {
            double mouseX = minecraft.mouseHandler.getScaledXPos(window);
            double mouseY = minecraft.mouseHandler.getScaledYPos(window);
            return inspectGui(screen, mouseX, mouseY);
        }

        return CursorType.DEFAULT;
    }

    /**
     * Recursively inspects a GUI element (and its children if it’s a container)
     * to determine the appropriate cursor type at the given mouse position.
     * <p>
     * Stops at the first hovered child, matching the default implementation of
     * {@link ContainerEventHandler#mouseClicked}.
     */
    public CursorType inspectGui(GuiEventListener element, double mouseX, double mouseY) {
        if (CursorTypeUtil.isHovered(element, mouseX, mouseY)) {
            if (element instanceof ContainerEventHandler container) {
                for (GuiEventListener child : container.children()) {
                    CursorType cursorType = inspectGui(child, mouseX, mouseY);
                    if (CursorTypeUtil.nonDefault(cursorType)) {
                        return cursorType;
                    }
                }
            }
            if (element instanceof CursorProvider provider) {
                debugRenderer.onInspect(element, mouseX, mouseY);
                return provider.cursors_extended$cursorType(mouseX, mouseY);
            }
            debugRenderer.onInspect(element, mouseX, mouseY);
        }
        return CursorType.DEFAULT;
    }

    public void setVisibleScreen(@Nullable Screen visibleScreen) {
        this.visibleScreen = visibleScreen;
    }

    public @Nullable Screen getVisibleScreen() {
        Screen screen = minecraft.screen;
        return screen != null ? screen : visibleScreen;
    }

    public void toggleVirtual() {
        cursorRenderer.resetCursor(minecraft.getWindow());
        cursorRenderer = isVirtual() ? new CursorRenderer.Native(registry) : new CursorRenderer.Virtual(registry);
    }

    public void renderCursor(Window window, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        cursorRenderer.render(window, minecraft, guiGraphics, mouseX, mouseY);
    }

    public boolean isVirtual() {
        return cursorRenderer instanceof CursorRenderer.Virtual;
    }

    public void toggleDebugger() {
        boolean debugging = isDebugging();
        debugRenderer.destroy();
        debugRenderer = debugging ? InspectorDebugRenderer.NOP : InspectorDebugRenderer.create();
    }

    public void renderDebugger(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        debugRenderer.render(minecraft, this::getVisibleScreen, guiGraphics, mouseX, mouseY);
    }

    public boolean isDebugging() {
        return debugRenderer.isActive();
    }

    public void applyCursor(Window window) {
        cursorRenderer.setCursor(window);
    }

    public Cursor getDisplayedCursor(Window window) {
        return cursorRenderer.getCurrentCursor(window);
    }

    public Cursor getDisplayedCursor() {
        return getDisplayedCursor(minecraft.getWindow());
    }

    public Window getWindow() {
        return minecraft.getWindow();
    }
}
