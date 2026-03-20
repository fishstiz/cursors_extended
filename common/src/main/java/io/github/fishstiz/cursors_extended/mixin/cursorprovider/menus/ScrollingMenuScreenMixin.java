package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = {LoomScreen.class, StonecutterScreen.class})
public abstract class ScrollingMenuScreenMixin {
    @ModifyArg(
            method = "extractBackground",
            slice = @Slice(from = @At(
                    value = "FIELD",
                    target = "Lcom/mojang/blaze3d/platform/cursor/CursorTypes;RESIZE_NS:Lcom/mojang/blaze3d/platform/cursor/CursorType;",
                    opcode = 178 // GETSTATIC
            )),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V",
                    ordinal = 0
            )
    )
    private CursorType onRequestCursor(CursorType cursorType) {
        return CursorTypeUtil.applyScrollbarConfig(cursorType);
    }
}
