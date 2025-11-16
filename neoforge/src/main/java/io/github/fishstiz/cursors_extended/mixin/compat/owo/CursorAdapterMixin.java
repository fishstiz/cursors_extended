package io.github.fishstiz.cursors_extended.mixin.compat.owo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.compat.CursorStateTracker;
import io.github.fishstiz.cursors_extended.compat.ModCursor;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import io.wispforest.owo.ui.util.CursorAdapter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CursorAdapter.class, remap = false)
public class CursorAdapterMixin {
    @Unique
    private static final String cursors_extended$OWO = "owo";

    @WrapOperation(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwCreateStandardCursor(I)J"
    ))
    private long trackStandardCursors(int shape, Operation<Long> original) {
        long handle = original.call(shape);

        if (handle != MemoryUtil.NULL) {
            CursorType cursorType = CursorTypeUtil.mapStandardCursor(shape);
            if (cursorType != null) {
                CursorStateTracker.get().trackCursor(new ModCursor(handle, cursors_extended$OWO, cursorType));
            }
        }

        return handle;
    }

    @Redirect(method = "applyStyle", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursor(JJ)V"
    ))
    private void setOwoCursor(long window, long cursor) {
        CursorStateTracker tracker = CursorStateTracker.get();

        if (cursor == MemoryUtil.NULL) {
            tracker.resetCursor(window, cursors_extended$OWO);
            cursors_extended$checkRemapOption(window, cursor, CursorType.DEFAULT, false);
            return;
        }

        ModCursor modCursor = tracker.getCursor(cursor);
        if (modCursor == null) {
            modCursor = ModCursor.createCustom(cursor, cursors_extended$OWO);
            tracker.trackCursor(modCursor);
        }

        tracker.setCursor(window, modCursor);
        cursors_extended$checkRemapOption(window, cursor, modCursor.cursorType(), modCursor.custom());
    }

    @Unique
    private static void cursors_extended$checkRemapOption(long window, long cursor, CursorType cursorType, boolean force) {
        if (force || !CursorsExtended.CONFIG.isRemapStandardCursors()) {
            GLFW.glfwSetCursor(window, cursor);
            CursorStateTracker.syncWithMinecraft(window, cursorType);
        }
    }
}
