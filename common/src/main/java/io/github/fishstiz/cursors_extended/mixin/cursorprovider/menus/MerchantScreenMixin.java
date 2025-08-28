package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreenMixin<MerchantMenu> {
    protected MerchantScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(method = "renderContents", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen$TradeOfferButton;isHoveredOrFocused()Z"
    ))
    private boolean setCursorOnHover(@Coerce Button instance, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphics guiGraphics) {
        if (instance.isHovered()) {
            if (instance.isActive()) {
                guiGraphics.requestCursor(CursorTypeUtil.canShift() ? CursorTypesExt.SHIFT : CursorTypes.POINTING_HAND);
            } else {
                guiGraphics.requestCursor(CursorTypes.NOT_ALLOWED);
            }
        }

        return original.call(instance);
    }
}
