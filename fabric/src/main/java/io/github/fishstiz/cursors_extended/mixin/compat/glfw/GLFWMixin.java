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

@SuppressWarnings("unused")
@Mixin(value = GLFW.class, remap = false, priority = 5000)
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
            return handle;
        }

        String name = CursorTypeUtil.mapStandardCursorName(shape);
        if (name != null) {
            String sourcePackage = CursorStateTracker.getStackWalker().walk(GLFWMixin::cursors_extended$getSourcePackage);
            CursorStateTracker.get().trackCursor(new ModCursor(handle, sourcePackage, name));
        }

        return handle;
    }

    @WrapMethod(method = "nglfwCreateCursor")
    private static long trackCustomCursor(long image, int xhot, int yhot, Operation<Long> original) {
        long handle = original.call(image, xhot, yhot);

        if (GLFWInternal.isCreatingCursor()) {
            return handle;
        }

        String sourcePackage = CursorStateTracker.getStackWalker().walk(GLFWMixin::cursors_extended$getSourcePackage);
        CursorStateTracker.get().trackCursor(ModCursor.createCustom(handle, sourcePackage));
        return handle;
    }

    @Inject(method = "glfwDestroyCursor", at = @At("RETURN"))
    private static void untrackCursor(long cursor, CallbackInfo ci) {
        ModCursor modCursor = CursorStateTracker.get().getCursor(cursor);
        if (modCursor != null) {
            CursorStateTracker.get().untrackCursor(modCursor);
            CursorsExtended.getInstance().getRegistry().unregisterCustom(modCursor.cursorType());
        }
    }

    @WrapMethod(method = "glfwSetCursor")
    private static void setMappedCursor(long window, long cursor, Operation<Void> original) {
        CursorStateTracker tracker = CursorStateTracker.get();
        ModCursor modCursor = tracker.getCursor(cursor);

        boolean internal = GLFWInternal.isSettingCursor();
        boolean reentry = GLFWInternal.consumeReentryCursor(window, cursor);

        if (modCursor == null || internal || reentry) {
            // When internal calls are async-dispatched to another thread due to other mods (ixeris), isSettingCursor flag
            // clears before the dispatched call re-enters.
            //
            // Mark (window, cursor) pairs to identify these reentrant calls.
            //
            // Only mark reentry when:
            // - isSettingCursor(): Internal call that might be async-dispatched
            // - modCursor != null: Tracked cursor (avoids polluting map with external untracked cursors)
            //
            // Stale entries are harmless since it doesn't change behavior (mostly) and is bounded by window count.
            //
            // This would NOT be necessary if these other mods used mixinextras instead to avoid recursion.
            //
            if (!reentry && (GLFWInternal.isSettingCursor() || modCursor != null)) {
                GLFWInternal.markReentryCursor(window, cursor);
            }
            original.call(window, cursor);
            return;
        }

        CursorRegistry registry = CursorsExtended.getInstance().getRegistry();
        if (cursor == MemoryUtil.NULL) {
            tracker.resetCursor(window, CursorStateTracker.getStackWalker().walk(GLFWMixin::cursors_extended$getSourcePackage));
            CursorStateTracker.syncWithMinecraft(window, CursorType.DEFAULT);
            original.call(window, CursorsExtended.CONFIG.isRemapStandardCursors() ? registry.get(CursorType.DEFAULT).handle() : MemoryUtil.NULL);
            return;
        }

        Cursor mapped = registry.get(modCursor.cursorType());
        if (!modCursor.custom() && !CursorsExtended.CONFIG.getOrCreateSettings(mapped).enabled()) {
            mapped = registry.get(CursorType.DEFAULT);
            tracker.resetCursor(window, modCursor.source());
        } else {
            tracker.setCursor(window, modCursor);
        }

        // main window handled by GameRenderer#render, which eventually goes through this method again as an internal call
        if (window != CursorsExtended.getInstance().getDisplay().getWindow().handle()) {
            CursorsExtended.getInstance().getLoader().lazyLoadTexture(mapped);
            original.call(window, mapped.handle());
        }
    }
}
