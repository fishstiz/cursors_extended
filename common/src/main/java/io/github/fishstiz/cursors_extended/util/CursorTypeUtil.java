package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CursorTypeUtil {
    private CursorTypeUtil() {
    }

    public static final Window WINDOW = Minecraft.getInstance().getWindow();

    public static boolean nameEquals(CursorType a, CursorType b) {
        return Objects.equals(a.toString(), b.toString());
    }

    public static boolean canShift() {
        return CursorsExtended.getInstance().getRegistry().get(CursorTypesExt.SHIFT).isTextureEnabled() &&
               (InputConstants.isKeyDown(WINDOW, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputConstants.isKeyDown(WINDOW, GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    public static boolean isLeftClickHeld() {
        return GLFW.glfwGetMouseButton(WINDOW.handle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
    }

    public static boolean isHeld(CursorType lastCursorType) {
        return CursorTypesExt.isHoldType(lastCursorType) &&
               CursorsExtended.getInstance().getRegistry().get(lastCursorType).isEnabled() &&
               CursorTypeUtil.isLeftClickHeld();
    }

    public static boolean nonDefault(CursorType cursorType) {
        return cursorType != null && !nameEquals(cursorType, CursorType.DEFAULT);
    }

    public static CursorType arrowIfDefault(CursorType cursorType) {
        return nameEquals(cursorType, CursorType.DEFAULT) ? CursorTypes.ARROW : cursorType;
    }

    public static boolean isHovered(GuiEventListener guiEventListener, double mouseX, double mouseY) {
        if (guiEventListener instanceof AbstractWidget widget) {
            return widget.visible &&
                   widget.isHovered() &&
                   mouseX >= widget.getX() &&
                   mouseY >= widget.getY() &&
                   mouseX < widget.getRight() &&
                   mouseY < widget.getBottom();
        }
        return guiEventListener.isMouseOver(mouseX, mouseY);
    }
}
