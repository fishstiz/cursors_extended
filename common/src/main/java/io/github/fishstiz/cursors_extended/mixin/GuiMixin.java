package io.github.fishstiz.cursors_extended.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.CursorDisplay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;applyCursor(Lcom/mojang/blaze3d/platform/Window;)V"
    ))
    private void renderCursor(
            DeltaTracker deltaTracker,
            boolean shouldRenderLevel,
            boolean resourcesLoaded,
            CallbackInfo ci,
            @Local(ordinal = 0) GuiGraphicsExtractor guiGraphics,
            @Local(ordinal = 0) int mouseX,
            @Local(ordinal = 1) int mouseY
    ) {
        CursorDisplay display = CursorsExtended.getInstance().getDisplay();
        display.renderDebugger(guiGraphics, mouseX, mouseY);
        display.renderCursor(minecraft.getWindow(), guiGraphics, mouseX, mouseY);
    }
}
