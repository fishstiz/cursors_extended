package io.github.fishstiz.cursors_extended.mixin.compat.glfw.internal;
//
//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import com.mojang.blaze3d.platform.cursor.CursorType;
//import io.github.fishstiz.cursors_extended.compat.glfw.GLFWInternal;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//
//@Mixin(CursorType.class)
//public abstract class CursorTypeMixin {
//    @WrapOperation(method = "createStandardCursor", at = @At(
//            value = "INVOKE",
//            target = "Lorg/lwjgl/glfw/GLFW;glfwCreateStandardCursor(I)J",
//            remap = false
//    ))
//    private static long createStandardCursorInternal(int shape, Operation<Long> original) {
//        return GLFWInternal.createStandardCursor(shape);
//    }
//
//    @WrapOperation(method = "select", at = @At(
//            value = "INVOKE",
//            target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursor(JJ)V",
//            remap = false
//    ))
//    private void setCursorInternal(long window, long cursor, Operation<Void> original) {
//        GLFWInternal.setCursor(window, cursor);
//    }
//}
