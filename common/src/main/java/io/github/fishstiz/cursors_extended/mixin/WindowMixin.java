package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.compat.CursorStateTracker;
import io.github.fishstiz.cursors_extended.compat.WindowCursor;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorDisplay;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Window.class)
public abstract class WindowMixin implements WindowCursor {
    @Shadow
    @Final
    private long handle;

    @Shadow
    public CursorType currentCursor;

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
            if (cursor.getTexture() instanceof CursorTexture.Animated animatedCursor) {
                animatedCursor.restartAnimation();
            }
            return true;
        }
        if (CursorsExtended.CONFIG.isAggressiveCursor()) {
            return true;
        }
        return cursor.handle() != cursors_extended$currentCursorHandle;
    }

    @ModifyVariable(method = "selectCursor", at = @At("HEAD"), argsOnly = true)
    private CursorType resolveSelected(CursorType value) {
        CursorType cursorType = cursors_extended$resolveCursor(value);
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(cursorType);
        CursorsExtended.getInstance().getLoader().lazyLoadTexture(cursor);

        return (cursor.hasTexture() && !cursor.isTextureEnabled())
               && !cursor.isCustom()
               && CursorsExtended.CONFIG.hasResourcePack()
                ? CursorType.DEFAULT
                : cursorType;
    }

    @WrapOperation(method = "selectCursor", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/cursor/CursorType;select()V"
    ))
    private void onSelect(CursorType instance, Operation<Void> original) {
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(instance);
        cursors_extended$currentCursorHandle = cursor.handle();
        CursorDisplay cursorDisplay = CursorsExtended.getInstance().getDisplay();
        cursorDisplay.applyCursor(cursorDisplay.getWindow());
    }

    @Override
    public void cursors_extended$setCurrentCursor(CursorType cursorType) {
        Cursor cursor = CursorsExtended.getInstance().getRegistry().get(cursorType);
        this.currentCursor = cursor.cursorType();
        this.cursors_extended$currentCursorHandle = cursor.handle();
    }

    @Unique
    private CursorType cursors_extended$resolveCursor(CursorType requestedCursor) {
        CursorStateTracker tracker = CursorStateTracker.get();
        if (tracker.isTracking()) {
            CursorType currentTrackedCursor = tracker.getCurrentCursor(this.handle);
            if (CursorTypeUtil.nonDefault(currentTrackedCursor)) {
                return currentTrackedCursor;
            }
        }
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
            Window self = (Window) (Object) this;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getWindow() != self || (minecraft.gui.overlay() != null && minecraft.gui.overlay().isPausing())) {
                return requestedCursor;
            }

            CursorType cursorType = CursorsExtended.getInstance().getDisplay().getCursorAt(self);
            if (CursorTypeUtil.nonDefault(cursorType)) {
                return cursorType;
            }
        }
        return CursorType.DEFAULT;
    }
}
