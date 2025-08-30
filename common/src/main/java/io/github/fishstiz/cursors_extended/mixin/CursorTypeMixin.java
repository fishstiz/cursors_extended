package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CursorType.class)
public abstract class CursorTypeMixin {
    @WrapOperation(method = "select", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursor(JJ)V"))
    private void onSelect(long window, long cursor, Operation<Void> original) {
        CursorType cursorType = (CursorType) (Object) this;
        if (CursorManager.INSTANCE.isRegistered(cursorType) && CursorManager.INSTANCE.isActive()) {
            CursorManager.INSTANCE.setCurrentCursor(cursorType);
        } else {
            original.call(window, cursor);
        }
    }

    @Inject(method = "createStandardCursor", at = @At("RETURN"))
    private static void registerStandardCursors(int shape, String name, CursorType fallback, CallbackInfoReturnable<CursorType> cir) {
        CursorType standardCursorType = cursors_extended$mapStandardCursor(shape);
        if (standardCursorType != null) {
            CursorsExtended.LOGGER.info("[cursors_extended] Registering an alias for {}: {}", standardCursorType, name);
            CursorManager.INSTANCE.registerAlias(standardCursorType.toString(), name);
        }
    }

    @Unique
    private static @Nullable CursorType cursors_extended$mapStandardCursor(int shape) {
        CursorsExtended.LOGGER.debug("Forcing static initialization. {}, {}", CursorTypes.ARROW, CursorType.DEFAULT);
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
