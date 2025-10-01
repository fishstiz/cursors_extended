package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorDisplay;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.cursor.TexturedCursorType;
import io.github.fishstiz.cursors_extended.resource.CursorTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
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

    static {
        CursorsExtended.LOGGER.debug("[cursors_extended] Initializing CursorTypes: {}", CursorTypes.class);
    }

    @Override
    public @Nullable CursorTexture cursors_extended$getTexture() {
        return cursors_extended$texture;
    }

    @Override
    public void cursors_extended$setTexture(CursorTexture texture) {
        CursorTexture previousTexture = cursors_extended$texture;
        this.cursors_extended$texture = texture;

        CursorDisplay cursorDisplay = CursorsExtended.getInstance().getDisplay();
        CursorRegistry cursorRegistry = CursorsExtended.getInstance().getRegistry();
        if (cursorDisplay.getDisplayedCursor() == cursorRegistry.get(((CursorType) (Object) this))) {
            select(cursorDisplay.getWindow());
        }

        if (previousTexture != null) {
            previousTexture.close();
        }
    }

    @WrapOperation(method = "select", at = @At(
            value = "FIELD",
            target = "Lcom/mojang/blaze3d/platform/cursor/CursorType;handle:J"
    ))
    private long onSetCursor(CursorType instance, Operation<Long> original) {
        if (cursors_extended$texture != null && cursors_extended$texture.handle() != MemoryUtil.NULL) {
            return cursors_extended$texture.handle();
        }

        return original.call(instance);
    }

    @Inject(method = "createStandardCursor", at = @At("RETURN"))
    private static void registerStandardCursors(int shape, String name, CursorType fallback, CallbackInfoReturnable<CursorType> cir) {
        if (cir.getReturnValue() != null) {
            CursorType standardCursorType = cursors_extended$mapStandardCursor(shape);
            if (standardCursorType != null) {
                CursorsExtended.LOGGER.info("[cursors_extended] Registering an alias for {}: {}", standardCursorType, name);
                CursorsExtended.getInstance().getRegistry().registerAlias(standardCursorType, cir.getReturnValue());
            }
        }
    }

    @Unique
    private static @Nullable CursorType cursors_extended$mapStandardCursor(int shape) {
        return switch (shape) {
            case GLFW.GLFW_ARROW_CURSOR -> CursorType.DEFAULT;
            case GLFW.GLFW_POINTING_HAND_CURSOR -> CursorTypes.POINTING_HAND;
            case GLFW.GLFW_IBEAM_CURSOR -> CursorTypes.IBEAM;
            case GLFW.GLFW_CROSSHAIR_CURSOR -> CursorTypes.CROSSHAIR;
            case GLFW.GLFW_RESIZE_EW_CURSOR -> CursorTypes.RESIZE_EW;
            case GLFW.GLFW_RESIZE_NS_CURSOR -> CursorTypes.RESIZE_NS;
            case GLFW.GLFW_RESIZE_NWSE_CURSOR -> CursorTypesExt.RESIZE_NWSE;
            case GLFW.GLFW_RESIZE_NESW_CURSOR -> CursorTypesExt.RESIZE_NESW;
            case GLFW.GLFW_RESIZE_ALL_CURSOR -> CursorTypes.RESIZE_ALL;
            case GLFW.GLFW_NOT_ALLOWED_CURSOR -> CursorTypes.NOT_ALLOWED;
            default -> null;
        };
    }
}
