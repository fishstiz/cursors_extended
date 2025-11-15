package io.github.fishstiz.cursors_extended.mixin.compat.glfw;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.compat.CursorStateTracker;
import io.github.fishstiz.cursors_extended.compat.ModCursor;
import io.github.fishstiz.cursors_extended.compat.glfw.GLFWInternal;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(value = GLFW.class, remap = false)
public abstract class GLFWMixin {
    @Unique
    private static final int MAX_SOURCE_PACKAGE_DEPTH = 4;

    static {
        CursorsExtended.LOGGER.debug("[cursors_extended] Loading CursorTypes: {}", CursorTypes.class);
    }

    @Unique
    private static String cursors_extended$getSourcePackage(Stream<StackWalker.StackFrame> frames) {
        return frames.dropWhile(frame -> frame.getDeclaringClass() == GLFW.class)
                .findFirst()
                .map(frame -> {
                    String packageName = frame.getDeclaringClass().getPackageName();
                    int count = 0, index = -1;
                    while ((index = packageName.indexOf('.', index + 1)) != -1) {
                        if (++count == MAX_SOURCE_PACKAGE_DEPTH) return packageName.substring(0, index);
                    }
                    return packageName;
                })
                .orElse("placeholder");
    }

    @WrapMethod(method = "glfwCreateStandardCursor")
    private static long trackStandardCursor(int shape, Operation<Long> original) {
        long handle = original.call(shape);

        if (GLFWInternal.isCreatingStandardCursor() || CursorStateTracker.get().getCursor(handle) != null) {
            GLFWInternal.trackInternalCursor(handle);
            return handle;
        }

        String mapped = CursorTypeUtil.mapStandardCursorName(shape);
        if (mapped != null) {
            String sourcePackage = CursorStateTracker.getStackWalker().walk(GLFWMixin::cursors_extended$getSourcePackage);
            CursorStateTracker.get().trackCursor(new ModCursor(handle, sourcePackage, new CursorType(mapped, handle)));
        }

        return handle;
    }

    @WrapMethod(method = "nglfwCreateCursor")
    private static long trackCustomCursor(long image, int xhot, int yhot, Operation<Long> original) {
        long handle = original.call(image, xhot, yhot);

        if (GLFWInternal.isCreatingCursor()) {
            GLFWInternal.trackInternalCursor(handle);
            return handle;
        }

        String sourcePackage = CursorStateTracker.getStackWalker().walk(GLFWMixin::cursors_extended$getSourcePackage);
        CursorStateTracker.get().trackCursor(ModCursor.ofUnknownType(handle, sourcePackage));
        CursorsExtended.LOGGER.info("[cursors_extended] Tracking custom cursor from '{}'", sourcePackage);
        return handle;
    }

    @Inject(method = "glfwDestroyCursor", at = @At("RETURN"))
    private static void untrackCursor(long cursor, CallbackInfo ci) {
        GLFWInternal.untrackInternalCursor(cursor);
        ModCursor modCursor = CursorStateTracker.get().getCursor(cursor);
        if (modCursor != null) {
            CursorStateTracker.get().untrackCursor(modCursor);
        }
    }

    @WrapMethod(method = "glfwSetCursor")
    private static void setMappedCursor(long window, long cursor, Operation<Void> original) {
        CursorStateTracker tracker = CursorStateTracker.get();

        if (GLFWInternal.isSettingCursor() || GLFWInternal.isInternalCursor(cursor) || !tracker.isTracking()) {
            original.call(window, cursor);
            return;
        }

        CursorRegistry registry = CursorsExtended.getInstance().getRegistry();
        if (cursor == MemoryUtil.NULL) {
            tracker.resetCursor(window, CursorStateTracker.getStackWalker().walk(GLFWMixin::cursors_extended$getSourcePackage));
            original.call(window, CursorsExtended.CONFIG.isRemapStandardCursors() ? registry.get(CursorType.DEFAULT).handle() : MemoryUtil.NULL);
            CursorStateTracker.syncWithMinecraft(window, CursorType.DEFAULT);
            return;
        }

        ModCursor modCursor = tracker.getCursor(cursor);
        if (modCursor == null) {
            original.call(window, cursor);
            return;
        }

        tracker.setCursor(window, modCursor);

        if (modCursor.custom() || !CursorsExtended.CONFIG.isRemapStandardCursors()) {
            original.call(window, cursor);
            CursorStateTracker.syncWithMinecraft(window, modCursor.cursorType());
            return;
        }

        Cursor mapped = registry.get(modCursor.cursorType());
        if (!mapped.isEnabled()) {
            mapped = registry.get(CursorType.DEFAULT);
        }

        if (window != CursorsExtended.getInstance().getDisplay().getWindow().handle()) {
            CursorsExtended.getInstance().getLoader().lazyLoadTexture(mapped);
            original.call(window, mapped.handle());
        }
    }
}
