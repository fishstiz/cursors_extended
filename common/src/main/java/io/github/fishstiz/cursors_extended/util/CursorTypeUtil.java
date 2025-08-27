package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CursorTypeUtil {
    private CursorTypeUtil() {
    }

    public static final long WINDOW = Minecraft.getInstance().getWindow().getWindow();

    public static boolean nameEquals(CursorType a, CursorType b) {
        return Objects.equals(a.toString(), b.toString());
    }

    public static boolean canShift() {
        return CursorManager.INSTANCE.isEnabled(CursorTypesExt.SHIFT) &&
               (InputConstants.isKeyDown(WINDOW, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputConstants.isKeyDown(WINDOW, GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    public static boolean isLeftClickHeld() {
        return GLFW.glfwGetMouseButton(WINDOW, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
    }

    public static boolean nonDefault(CursorType cursorType) {
        return cursorType != null && cursorType != CursorType.DEFAULT;
    }

    public static CursorType firstNonDefault(CursorType... cursorTypes) {
        for (CursorType cursorType : cursorTypes) {
            if (nonDefault(cursorType)) {
                return cursorType;
            }
        }
        return CursorType.DEFAULT;
    }

    public static boolean isHovered(GuiEventListener guiEventListener, double mouseX, double mouseY) {
        if (guiEventListener instanceof AbstractWidget widget) {
            return widget.visible && (widget.isHovered() || widget.isMouseOver(mouseX, mouseY));
        }
        return guiEventListener.isMouseOver(mouseX, mouseY);
    }
}
