package io.github.fishstiz.cursors_extended.mixin.cursorprovider.menus;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorTickController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.StonecutterMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StonecutterScreen.class)
public abstract class StonecutterScreenMixin extends AbstractContainerScreenMixin<StonecutterMenu> {
    @Shadow
    @Final
    private static ResourceLocation RECIPE_HIGHLIGHTED_SPRITE;

    protected StonecutterScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(method = "renderButtons", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"
    ))
    private void setPointerOnHover(GuiGraphics instance, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int x, int y, int width, int height, Operation<Void> original) {
        original.call(instance, renderPipeline, resourceLocation, x, y, width, height);
        if (CursorsExtended.CONFIG.isStonecutterRecipesEnabled() && resourceLocation == RECIPE_HIGHLIGHTED_SPRITE) {
            CursorTickController.INSTANCE.setTickCursor(CursorTypes.POINTING_HAND);
        }
    }
}
