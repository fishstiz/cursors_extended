package io.github.fishstiz.cursors_extended.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.compat.CursorStateTracker;
import io.github.fishstiz.cursors_extended.compat.ModCursor;
import io.github.fishstiz.cursors_extended.services.SDLMouseOpsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CursorType.class)
public class CursorTypeMixin {
    // a mod may create a custom cursor and use blaze3D CursorType for compat (waow), therefore it doesn't need to be tracked
    @Inject(method = "<init>", at = @At("TAIL"))
    private void untrackCursor(String name, long handle, CallbackInfo ci) {
        ModCursor modCursor = CursorStateTracker.get().getCursor(handle);
        if (modCursor != null) {
            CursorStateTracker.get().untrackCursor(modCursor);
        }
    }

    @WrapOperation(method = "select", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLMouse;SDL_SetCursor(J)Z"))
    private boolean handleSetCursor(long cursor, Operation<Boolean> original) {
        SDLMouseOpsHandler.INSTANCE.beforeSetCursor(cursor);
        boolean success = original.call(cursor);
        SDLMouseOpsHandler.INSTANCE.afterSetCursor(cursor);
        return success;
    }

    @WrapOperation(method = "createStandardCursor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLMouse;SDL_CreateSystemCursor(I)J"))
    private static long handleCreateStandardCursor(int id, Operation<Long> original) {
        SDLMouseOpsHandler.INSTANCE.beforeCreateStandardCursor(id);
        long handle = original.call(id);
        SDLMouseOpsHandler.INSTANCE.afterCreateStandardCursor(handle, id);
        return handle;
    }
}
