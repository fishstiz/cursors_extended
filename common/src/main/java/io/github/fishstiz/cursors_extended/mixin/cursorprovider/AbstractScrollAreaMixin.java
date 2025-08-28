package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.components.AbstractScrollArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin {
    @ModifyArg(method = "renderScrollbar", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V"
    ))
    private CursorType holdResizeNs(CursorType cursorType) {
        if (CursorTypeUtil.nameEquals(cursorType, CursorTypes.RESIZE_NS)) {
            return CursorsExtended.CONFIG.isResizeScrollbarEnabled() ? CursorTypesExt.RESIZE_NS_HOLD : CursorType.DEFAULT;
        }
        if (CursorTypeUtil.nameEquals(cursorType, CursorTypes.POINTING_HAND)) {
            return CursorsExtended.CONFIG.isPointerScrollbarEnabled() ? cursorType : CursorType.DEFAULT;
        }
        return cursorType;
    }
}
