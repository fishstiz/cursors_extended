package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.InputConstants;
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

    public static boolean nameEquals(CursorType a, CursorType b) {
        return Objects.equals(a.toString(), b.toString());
    }

    public static boolean canShift() {
        Cursor shiftCursor = CursorsExtended.getInstance().getRegistry().get(CursorTypesExt.SHIFT);
        return CursorsExtended.CONFIG.getOrCreateSettings(shiftCursor).enabled() &&
               (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    public static boolean isLeftClickHeld() {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
    }

    public static boolean isHeld(CursorType lastCursorType) {
        return CursorTypesExt.isHoldType(lastCursorType) &&
               CursorsExtended.CONFIG.getOrCreateSettings(CursorsExtended.getInstance().getRegistry().get(lastCursorType)).enabled() &&
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

    public static @Nullable String mapStandardCursorName(int shape) {
        return switch (shape) {
            case GLFW.GLFW_ARROW_CURSOR -> "default";
            case GLFW.GLFW_POINTING_HAND_CURSOR -> "pointing_hand";
            case GLFW.GLFW_IBEAM_CURSOR -> "ibeam";
            case GLFW.GLFW_CROSSHAIR_CURSOR -> "crosshair";
            case GLFW.GLFW_RESIZE_EW_CURSOR -> "resize_ew";
            case GLFW.GLFW_RESIZE_NS_CURSOR -> "resize_ns";
            case GLFW.GLFW_RESIZE_NWSE_CURSOR -> "resize_nwse";
            case GLFW.GLFW_RESIZE_NESW_CURSOR -> "resize_nesw";
            case GLFW.GLFW_RESIZE_ALL_CURSOR -> "resize_all";
            case GLFW.GLFW_NOT_ALLOWED_CURSOR -> "not_allowed";
            default -> null;
        };
    }
}
