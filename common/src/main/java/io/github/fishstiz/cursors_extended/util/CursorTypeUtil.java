package io.github.fishstiz.cursors_extended.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLMouse;

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
               (InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT));
    }

    public static boolean isLeftClickHeld() {
        return (SDLMouse.SDL_GetMouseState(null, null) & SDLMouse.SDL_BUTTON_LMASK) != 0;
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

    public static CursorType applyScrollbarConfig(CursorType cursorType) {
        if (nameEquals(cursorType, CursorTypes.RESIZE_NS)) {
            return CursorsExtended.CONFIG.isResizeScrollbarEnabled() ? CursorTypesExt.RESIZE_NS_HOLD : CursorType.DEFAULT;
        }
        if (nameEquals(cursorType, CursorTypes.POINTING_HAND)) {
            return CursorsExtended.CONFIG.isPointerScrollbarEnabled() ? cursorType : CursorType.DEFAULT;
        }
        return cursorType;
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
            case SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT -> CursorType.DEFAULT;
            case SDLMouse.SDL_SYSTEM_CURSOR_POINTER -> CursorTypes.POINTING_HAND;
            case SDLMouse.SDL_SYSTEM_CURSOR_TEXT -> CursorTypes.IBEAM;
            case SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR -> CursorTypes.CROSSHAIR;
            case SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE -> CursorTypes.RESIZE_EW;
            case SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE -> CursorTypes.RESIZE_NS;
            case SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE -> CursorTypesExt.RESIZE_NWSE;
            case SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE -> CursorTypesExt.RESIZE_NESW;
            case SDLMouse.SDL_SYSTEM_CURSOR_MOVE -> CursorTypes.RESIZE_ALL;
            case SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED -> CursorTypes.NOT_ALLOWED;
            case SDLMouse.SDL_SYSTEM_CURSOR_WAIT, SDLMouse.SDL_SYSTEM_CURSOR_PROGRESS -> CursorTypesExt.BUSY;
            default -> null;
        };
    }

    // this is to avoid loading and creating the cursor types prematurely
    public static @Nullable String mapStandardCursorName(int shape) {
        return switch (shape) {
            case SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT -> "default";
            case SDLMouse.SDL_SYSTEM_CURSOR_POINTER -> "pointing_hand";
            case SDLMouse.SDL_SYSTEM_CURSOR_TEXT -> "ibeam";
            case SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR -> "crosshair";
            case SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE -> "resize_ew";
            case SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE -> "resize_ns";
            case SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE -> "resize_nwse";
            case SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE -> "resize_nesw";
            case SDLMouse.SDL_SYSTEM_CURSOR_MOVE -> "resize_all";
            case SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED -> "not_allowed";
            case SDLMouse.SDL_SYSTEM_CURSOR_WAIT, SDLMouse.SDL_SYSTEM_CURSOR_PROGRESS -> "busy";
            default -> null;
        };
    }

//    public static @Nullable String mapStandardCursorName(int shape) {
//        return switch (shape) {
//            case GLFW.GLFW_ARROW_CURSOR -> "default";
//            case GLFW.GLFW_POINTING_HAND_CURSOR -> "pointing_hand";
//            case GLFW.GLFW_IBEAM_CURSOR -> "ibeam";
//            case GLFW.GLFW_CROSSHAIR_CURSOR -> "crosshair";
//            case GLFW.GLFW_RESIZE_EW_CURSOR -> "resize_ew";
//            case GLFW.GLFW_RESIZE_NS_CURSOR -> "resize_ns";
//            case GLFW.GLFW_RESIZE_NWSE_CURSOR -> "resize_nwse";
//            case GLFW.GLFW_RESIZE_NESW_CURSOR -> "resize_nesw";
//            case GLFW.GLFW_RESIZE_ALL_CURSOR -> "resize_all";
//            case GLFW.GLFW_NOT_ALLOWED_CURSOR -> "not_allowed";
//            default -> null;
//        };
//    }
}
