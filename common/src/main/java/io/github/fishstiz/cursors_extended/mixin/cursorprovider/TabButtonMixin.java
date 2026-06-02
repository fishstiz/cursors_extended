package io.github.fishstiz.cursors_extended.mixin.cursorprovider;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.cursor.CursorProvider;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MenuTabBar.MenuTabButton.class)
public abstract class TabButtonMixin extends TabButton implements CursorProvider {
    public TabButtonMixin(TabManager tabManager, Tab tab, int width, int height) {
        super(tabManager, tab, width, height);
    }

    @WrapWithCondition(method = "extractWidgetRenderState", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/tabs/MenuTabBar$MenuTabButton;handleCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"
    ))
    private boolean shouldHandleCursor(MenuTabBar.MenuTabButton instance, GuiGraphicsExtractor graphics) {
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
