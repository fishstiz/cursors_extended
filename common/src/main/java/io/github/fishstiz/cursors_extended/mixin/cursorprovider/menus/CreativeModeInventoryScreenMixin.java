package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreenMixin<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow
    private boolean scrolling;

    @Shadow
    @Nullable
    private Slot destroyItemSlot;

    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    protected abstract boolean insideScrollbar(double mouseX, double mouseY);

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

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void forceDefaultOnScroll(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (selectedTab.canScroll()) {
            if (this.scrolling && CursorTypeUtil.isLeftClickHeld() && CursorsExtended.CONFIG.isResizeScrollbarEnabled()) {
                guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
            } else if (this.insideScrollbar(mouseX, mouseY) && CursorsExtended.CONFIG.isPointerScrollbarEnabled()) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }
        }
    }

    @Inject(method = "checkTabHovering", at = @At("RETURN"))
    private void setPointerOnHover(GuiGraphics guiGraphics, CreativeModeTab creativeModeTab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (CursorsExtended.CONFIG.isCreativeTabsEnabled()
            && creativeModeTab != selectedTab
            && cir.getReturnValue()
            && this.cursors_extended$cursorType(mouseX, mouseY) == CursorType.DEFAULT) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }
}
