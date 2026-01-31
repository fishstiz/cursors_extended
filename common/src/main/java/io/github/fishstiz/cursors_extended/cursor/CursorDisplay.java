package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.debug.CursorDebugRenderer;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class CursorDisplay {
    private final CursorRegistry registry;
    private Minecraft minecraft;
    private CursorDebugRenderer debugRenderer = CursorDebugRenderer.NOP;
    private CursorRenderer cursorRenderer;
    private @Nullable Screen visibleScreen;

    public CursorDisplay(CursorRegistry registry) {
        this.registry = registry;
        this.cursorRenderer = new CursorRenderer.Native(registry);
    }

    public void onClientStarted(Minecraft minecraft) {
        this.minecraft = minecraft;

        if (CursorsExtended.CONFIG.isVirtualMode() != isVirtual()) {
            this.toggleVirtual();
        }
    }

    public CursorType getCursorAt(Window window) {
        Screen screen = getVisibleScreen();

        if (screen != null) {
            double mouseX = minecraft.mouseHandler.getScaledXPos(window);
            double mouseY = minecraft.mouseHandler.getScaledYPos(window);
            return getCursorAt(screen, mouseX, mouseY);
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
    public CursorType getCursorAt(GuiEventListener element, double mouseX, double mouseY) {
        if (CursorTypeUtil.isHovered(element, mouseX, mouseY)) {
            if (element instanceof ContainerEventHandler container) {
                for (GuiEventListener child : container.children()) {
                    CursorType cursorType = getCursorAt(child, mouseX, mouseY);
                    if (CursorTypeUtil.nonDefault(cursorType)) {
                        return cursorType;
                    }
                }
            }
            if (element instanceof CursorProvider provider) {
                debugRenderer.setLastCursorAt(element, mouseX, mouseY);
                return provider.cursors_extended$cursorType(mouseX, mouseY);
            }
            debugRenderer.setLastCursorAt(element, mouseX, mouseY);
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
        cursorRenderer.applyCursor(minecraft.getWindow());
    }

    public void renderCursor(Window window, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        cursorRenderer.render(window, minecraft, guiGraphics, mouseX, mouseY);
    }

    public boolean isVirtual() {
        return cursorRenderer instanceof CursorRenderer.Virtual;
    }

    public void toggleDebugger() {
        debugRenderer = debugRenderer.isActive() ? CursorDebugRenderer.NOP : CursorDebugRenderer.create();
    }

    public void renderDebugger(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        debugRenderer.render(minecraft, this::getVisibleScreen, guiGraphics, mouseX, mouseY);
    }

    public boolean isDebugging() {
        return debugRenderer.isActive();
    }

    public void applyCursor(Window window) {
        cursorRenderer.applyCursor(window);
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
