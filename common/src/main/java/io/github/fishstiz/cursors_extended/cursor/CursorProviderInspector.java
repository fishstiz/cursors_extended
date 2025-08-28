package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class CursorProviderInspector {
    public static final CursorProviderInspector INSTANCE = new CursorProviderInspector();
    private ElementInspector inspector = ElementInspector.NO_OP;

    private CursorProviderInspector() {
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

    public ElementInspector getInspector() {
        return inspector;
    }

    public void toggleInspector() {
        inspector.destroy();
        inspector = this.inspector == ElementInspector.NO_OP ? new ElementInspectorImpl() : ElementInspector.NO_OP;
    }
}
