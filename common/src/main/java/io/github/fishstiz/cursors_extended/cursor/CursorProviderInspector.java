package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
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
    private ElementInspector inspector = ElementInspector.NO_OP;
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
     * {@link ContainerEventHandler#mouseClicked(double, double, int, boolean)}.
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
                inspector.setInspected(element, mouseX, mouseY);
                return provider.cursors_extended$cursorType(mouseX, mouseY);
            }
        }
        inspector.setInspected(element, mouseX, mouseY);
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

    public ElementInspector getInspector() {
        return inspector;
    }

    public void toggleInspector() {
        inspector.destroy();
        inspector = this.inspector == ElementInspector.NO_OP ? new ElementInspectorImpl() : ElementInspector.NO_OP;
    }

    public void renderInspector(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        CursorProviderInspector.INSTANCE.getInspector().render(minecraft, screen, guiGraphics, mouseX, mouseY);
    }
}
