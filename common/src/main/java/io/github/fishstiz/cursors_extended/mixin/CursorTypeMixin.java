package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorDisplay;
import io.github.fishstiz.cursors_extended.cursor.TexturedCursorType;
import io.github.fishstiz.cursors_extended.resource.texture.CursorTexture;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CursorType.class)
public abstract class CursorTypeMixin implements TexturedCursorType {
    @Shadow
    public abstract void select(Window window);

    @Unique
    private CursorTexture cursors_extended$texture;

    @Unique
    private boolean cursors_extended$custom;

    static {
        CursorsExtended.LOGGER.debug("[cursors_extended] Loading CursorTypes: {}", CursorTypes.class);
    }

    @Override
    public @Nullable CursorTexture cursors_extended$getTexture() {
        return cursors_extended$texture;
    }

    @Unique
    private Cursor cursors_extended$getKey() {
        return CursorsExtended.getInstance().getRegistry().get((CursorType) (Object) this);
    }

    @Override
    public void cursors_extended$setTexture(CursorTexture texture) {
        CursorTexture previousTexture = cursors_extended$texture;
        this.cursors_extended$texture = texture;

        CursorDisplay cursorDisplay = CursorsExtended.getInstance().getDisplay();
        if (cursorDisplay.getDisplayedCursor() == cursors_extended$getKey()) {
            select(cursorDisplay.getWindow());
        }

        if (previousTexture != null && previousTexture != texture) {
            previousTexture.close();
        }
    }

    @Override
    public void cursors_extended$setCustom(boolean custom) {
        this.cursors_extended$custom = custom;
    }

    @Override
    public boolean cursors_extended$isCustom() {
        return this.cursors_extended$custom;
    }

    @WrapOperation(method = "select", at = @At(
            value = "FIELD",
            target = "Lcom/mojang/blaze3d/platform/cursor/CursorType;handle:J",
            opcode = Opcodes.GETFIELD
    ))
    private long onSetCursor(CursorType instance, Operation<Long> original) {
        if (!cursors_extended$isCustom() &&
            cursors_extended$texture != null &&
            cursors_extended$texture.handle() != MemoryUtil.NULL &&
            CursorsExtended.CONFIG.getOrCreateSettings(cursors_extended$getKey()).enabled()) {
            return cursors_extended$texture.handle();
        }
        return original.call(instance);
    }

    @Inject(method = "createStandardCursor", at = @At("RETURN"))
    private static void registerStandardCursors(int shape, String name, CursorType fallback, CallbackInfoReturnable<CursorType> cir) {
        if (cir.getReturnValue() != null) {
            CursorType standardCursorType = CursorTypeUtil.mapStandardCursor(shape);
            if (standardCursorType != null) {
                CursorsExtended.LOGGER.info("[cursors_extended] Registering an alias for {}: {}", standardCursorType, name);
                CursorsExtended.getInstance().getRegistry().registerAlias(standardCursorType, cir.getReturnValue());
            }
        }
    }
}
