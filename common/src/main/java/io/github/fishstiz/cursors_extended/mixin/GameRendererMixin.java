package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.fishstiz.cursors_extended.cursor.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderDeferredSubtitles()V"))
    private void renderCursor(
            DeltaTracker deltaTracker,
            boolean renderLevel,
            CallbackInfo ci,
            @Local(ordinal = 0) GuiGraphics guiGraphics,
            @Local(ordinal = 0) int mouseX,
            @Local(ordinal = 1) int mouseY
    ) {
        CursorManager.INSTANCE.renderCursor(minecraft, guiGraphics, mouseX, mouseY);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;applyCursor(Lcom/mojang/blaze3d/platform/Window;)V"))
    private void resolveCursor(
            DeltaTracker deltaTracker,
            boolean renderLevel,
            CallbackInfo ci,
            @Local(ordinal = 0) int mouseX,
            @Local(ordinal = 1) int mouseY
    ) {
        if (minecraft.screen != null) {
            CursorResolver.INSTANCE.beforeApplyCursor(minecraft, minecraft.screen, mouseX, mouseY);
        }
    }
}
