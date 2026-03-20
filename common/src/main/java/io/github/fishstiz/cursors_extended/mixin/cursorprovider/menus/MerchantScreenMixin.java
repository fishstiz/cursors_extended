package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreenMixin<MerchantMenu> {
    protected MerchantScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(method = "extractContents", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen$TradeOfferButton;isHoveredOrFocused()Z"
    ))
    private boolean setCursorOnHover(@Coerce Button instance, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphicsExtractor guiGraphics) {
        if (instance.isHovered()) {
            if (instance.isActive()) {
                guiGraphics.requestCursor(CursorTypeUtil.canShift() && CursorsExtended.CONFIG.isLegacyMode()
                        ? CursorTypesExt.SHIFT
                        : CursorTypes.POINTING_HAND
                );
            } else {
                guiGraphics.requestCursor(CursorTypes.NOT_ALLOWED);
            }
        }

        return original.call(instance);
    }

    @ModifyArg(method = "extractScroller", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;requestCursor(Lcom/mojang/blaze3d/platform/cursor/CursorType;)V"
    ))
    private CursorType onRequestCursor(CursorType cursor) {
        return CursorTypeUtil.applyScrollbarConfig(cursor);
    }
}
