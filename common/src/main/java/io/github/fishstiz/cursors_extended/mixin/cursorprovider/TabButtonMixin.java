package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TabButton.class)
public abstract class TabButtonMixin extends AbstractWidget implements CursorProvider {
    @Shadow
    public abstract boolean isSelected();

    protected TabButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @WrapWithCondition(method = "renderWidget", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/TabButton;handleCursor(Lnet/minecraft/client/gui/GuiGraphics;)V"
    ))
    private boolean shouldHandleCursor(TabButton instance, GuiGraphics guiGraphics) {
        return !instance.isSelected();
    }

    @Override
    public CursorType cursors_extended$cursorType(double mouseX, double mouseY) {
        if (this.isActive() && !this.isSelected()) {
            return CursorTypes.POINTING_HAND;
        }

        return CursorProvider.super.cursors_extended$cursorType(mouseX, mouseY);
    }
}
