package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.inspector.CursorProviderInspector;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Shadow
    private CursorType currentCursor;

    @Shadow
    private boolean allowCursorChanges;

    @WrapMethod(method = "selectCursor")
    private void onSelectCursor(CursorType cursorType, Operation<Void> original) {
        if (!CursorManager.INSTANCE.isActive()) {
            original.call(cursorType);
            return;
        }

        CursorType resolvedCursor = cursors_extended$resolveCursor(cursorType);
        CursorManager.INSTANCE.setCurrentCursor(resolvedCursor);
        this.currentCursor = resolvedCursor;
    }

    @Unique
    private CursorType cursors_extended$resolveCursor(CursorType requestedCursor) {
        if (!this.allowCursorChanges) {
            return CursorTypes.ARROW;
        }
        if (CursorTypeUtil.isHeld(this.currentCursor)) {
            return this.currentCursor;
        }
        if (CursorTypeUtil.nonDefault(requestedCursor)) {
            return requestedCursor;
        }
        if (CursorsExtended.CONFIG.isLegacyMode()) {
            CursorType inspected = CursorProviderInspector.INSTANCE.inspect();
            if (CursorTypeUtil.nonDefault(inspected)) {
                return inspected;
            }
        }
        return CursorType.DEFAULT;
    }
}
