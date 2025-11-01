package io.github.fishstiz.cursors_extended.mixin.compat.glfw.internal;

import io.github.fishstiz.cursors_extended.compat.glfw.GLFWInternal;
import io.github.fishstiz.cursors_extended.util.NativeImageUtil;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = NativeImageUtil.class, remap = false)
public abstract class NativeImageUtilMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "createCursor", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwCreateCursor(Lorg/lwjgl/glfw/GLFWImage;II)J"
    ))
    private static long createCursorInternal(GLFWImage image, int xhot, int yhot) {
        return GLFWInternal.createCursor(image, xhot, yhot);
    }
}
