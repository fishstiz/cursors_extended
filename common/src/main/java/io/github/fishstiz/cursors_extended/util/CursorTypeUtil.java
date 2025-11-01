package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CursorTypeUtil {
    private CursorTypeUtil() {
    }

    private static final Window WINDOW = Minecraft.getInstance().getWindow();

    public static boolean nameEquals(CursorType a, CursorType b) {
        return Objects.equals(a.toString(), b.toString());
    }

    public static boolean canShift() {
        Cursor shiftCursor = CursorsExtended.getInstance().getRegistry().get(CursorTypesExt.SHIFT);
        return shiftCursor.isEnabled() &&
               CursorsExtended.CONFIG.getOrCreateSettings(shiftCursor).enabled() &&
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

    public static @Nullable CursorType mapStandardCursor(int shape) {
        return switch (shape) {
            case GLFW.GLFW_ARROW_CURSOR -> CursorType.DEFAULT;
            case GLFW.GLFW_POINTING_HAND_CURSOR -> CursorTypes.POINTING_HAND;
            case GLFW.GLFW_IBEAM_CURSOR -> CursorTypes.IBEAM;
            case GLFW.GLFW_CROSSHAIR_CURSOR -> CursorTypes.CROSSHAIR;
            case GLFW.GLFW_RESIZE_EW_CURSOR -> CursorTypes.RESIZE_EW;
            case GLFW.GLFW_RESIZE_NS_CURSOR -> CursorTypes.RESIZE_NS;
            case GLFW.GLFW_RESIZE_NWSE_CURSOR -> CursorTypesExt.RESIZE_NWSE;
            case GLFW.GLFW_RESIZE_NESW_CURSOR -> CursorTypesExt.RESIZE_NESW;
            case GLFW.GLFW_RESIZE_ALL_CURSOR -> CursorTypes.RESIZE_ALL;
            case GLFW.GLFW_NOT_ALLOWED_CURSOR -> CursorTypes.NOT_ALLOWED;
            default -> null;
        };
    }
}
