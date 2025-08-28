package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.CursorTickController;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CursorType.class)
public abstract class CursorTypeMixin {
    @WrapOperation(method = "select", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursor(JJ)V"))
    private void onSelect(long window, long cursor, Operation<Void> original) {
        CursorType cursorType = (CursorType) (Object) this;
        if (CursorManager.INSTANCE.isRegistered(cursorType) && CursorManager.INSTANCE.isActive()) {
            CursorTickController.INSTANCE.setFallbackTickCursor(cursorType);
        } else {
            original.call(window, cursor);
        }
    }

    @Inject(method = "createStandardCursor", at = @At("HEAD"))
    private static void registerStandardCursors(int shape, String name, CursorType cursorType, CallbackInfoReturnable<CursorType> cir) {
        if (CursorTypeUtil.mapGLFWCursors(shape) != null) {
            CursorManager.INSTANCE.registerType(cursorType);
        }
    }
}
