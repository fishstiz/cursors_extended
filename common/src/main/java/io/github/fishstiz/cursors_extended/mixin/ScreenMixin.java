package io.github.fishstiz.cursors_extended.mixin;

import io.github.fishstiz.cursors_extended.cursor.CursorResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    @Nullable
    protected Minecraft minecraft;

    @Inject(method = "render", at = @At("RETURN"))
    private void afterScreenRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.minecraft != null) {
            CursorResolver.INSTANCE.afterRenderScreen(this.minecraft, (Screen) (Object) this, guiGraphics, mouseX, mouseY);
        }
    }
}
