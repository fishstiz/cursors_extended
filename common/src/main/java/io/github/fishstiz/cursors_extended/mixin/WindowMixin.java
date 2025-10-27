package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.resource.texture.AnimatedCursorTexture;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Shadow
    private CursorType currentCursor;

    @Shadow
    private boolean allowCursorChanges;
    @Unique
    private long cursors_extended$currentCursorHandle;

    @Definition(id = "currentCursor", field = "Lcom/mojang/blaze3d/platform/Window;currentCursor:Lcom/mojang/blaze3d/platform/cursor/CursorType;")
    @Definition(id = "cursorType", local = @Local(type = CursorType.class, ordinal = 1))
    @Expression("this.currentCursor != cursorType")
    @ModifyExpressionValue(method = "selectCursor", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean checkHasChanged(boolean original, @Local(ordinal = 1) CursorType cursorType) {
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(cursorType);
        if (original) {
            if (cursor.getTexture() instanceof AnimatedCursorTexture animatedCursorTexture) {
                animatedCursorTexture.restartAnimation();
            }
            return true;
        }
        if (CursorsExtended.CONFIG.isAggressiveCursor()) {
            return true;
        }
        return cursor.handle() != cursors_extended$currentCursorHandle;
    }

    @WrapMethod(method = "selectCursor")
    private void resolveSelectedCursor(CursorType requestedCursorType, Operation<Void> original) {
        CursorType cursorType = cursors_extended$resolveCursor(requestedCursorType);
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(cursorType);

        if (cursor.isEnabled() && cursor.isLazy()) {
            CursorsExtended.getInstance().getLoader().loadTexture(cursor);
        }

        original.call(cursor.isEnabled() ? cursorType : CursorType.DEFAULT);
    }

    @WrapOperation(method = "selectCursor", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/cursor/CursorType;select(Lcom/mojang/blaze3d/platform/Window;)V"
    ))
    private void onSelect(CursorType instance, Window window, Operation<Void> original) {
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(instance);
        cursors_extended$currentCursorHandle = cursor.handle();
        CursorsExtended.getInstance().getDisplay().applyCursor(window);
    }

    @Unique
    private CursorType cursors_extended$resolveCursor(CursorType requestedCursor) {
        if (!allowCursorChanges) {
            return CursorType.DEFAULT;
        }
        if (CursorTypeUtil.isHeld(this.currentCursor)) {
            return this.currentCursor;
        }
        if (CursorTypeUtil.nonDefault(requestedCursor)) {
            return requestedCursor;
        }
        if (CursorsExtended.CONFIG.isLegacyMode()) {
            CursorType cursorType = CursorsExtended.getInstance().getDisplay().getCursorAt((Window) (Object) this);
            if (CursorTypeUtil.nonDefault(cursorType)) {
                return cursorType;
            }
        }
        return CursorType.DEFAULT;
    }
}
