package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.components.AbstractScrollArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin {
    @ModifyArg(method = "extractScrollbar", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V"
    ))
    private CursorType holdResizeNs(CursorType cursorType) {
        return CursorTypeUtil.applyScrollbarConfig(cursorType);
    }
}
