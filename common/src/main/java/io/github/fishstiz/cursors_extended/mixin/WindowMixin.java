package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.CursorTickController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Window.class)
public class WindowMixin {
    @Shadow
    private CursorType currentCursor;

    @WrapMethod(method = "selectCursor")
    private void onSelectCursor(CursorType cursorType, Operation<Void> original) {
        if (!CursorManager.INSTANCE.isActive()) {
            original.call(cursorType);
            return;
        }

        if (CursorManager.INSTANCE.isRegistered(cursorType)) {
            CursorTickController.INSTANCE.setFallbackTickCursor(cursorType);
        } else {
            CursorTickController.INSTANCE.setTickCursor(cursorType);
        }
        
        this.currentCursor = CursorManager.INSTANCE.getAppliedCursor().getType();
    }
}
