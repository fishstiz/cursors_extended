package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreenMixin<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow
    private Slot destroyItemSlot;

    protected CreativeModeInventoryScreenMixin(Component title) {
        super(title);
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (CursorTypeUtil.canShift()
            && this.hoveredSlot != null
            && this.hoveredSlot == this.destroyItemSlot) {
            return CursorTypesExt.SHIFT;
        }
        return super.cursors_extended$cursorType(mouseX, mouseY);
    }

    @ModifyArg(
            method = "renderBg",
            slice = @Slice(from = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;insideScrollbar(DD)Z"
            )),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V",
                    ordinal = 0
            )
    )
    private CursorType onRequestCursor(CursorType cursorType) {
        return CursorTypeUtil.applyScrollbarConfig(cursorType);
    }
}
